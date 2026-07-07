package com.kiniot.uflex.api.iam.application.internal.eventhandlers;

import com.kiniot.uflex.api.iam.application.internal.outboundservices.hashing.HashingService;
import com.kiniot.uflex.api.iam.domain.model.aggregates.User;
import com.kiniot.uflex.api.iam.domain.model.commands.SeedRolesCommand;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Password;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.RoleName;
import com.kiniot.uflex.api.iam.domain.services.RoleCommandService;
import com.kiniot.uflex.api.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.kiniot.uflex.api.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class ApplicationReadyEventHandler {

    private final RoleCommandService roleCommandService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final String developerEmail;
    private final String developerPassword;
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationReadyEventHandler.class);

    public ApplicationReadyEventHandler(
            RoleCommandService roleCommandService,
            UserRepository userRepository,
            RoleRepository roleRepository,
            HashingService hashingService,
            @Value("${uflex.developer.email:}") String developerEmail,
            @Value("${uflex.developer.password:}") String developerPassword
    ) {
        this.roleCommandService = roleCommandService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.developerEmail = developerEmail;
        this.developerPassword = developerPassword;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        var applicationName = event.getApplicationContext().getId();
        LOGGER.info("Starting to verify if roles seeding is needed for {} at {}", applicationName, currentTimestamp());
        roleCommandService.handle(new SeedRolesCommand());
        LOGGER.info("Roles seeding verification completed for {} at {}", applicationName, currentTimestamp());
        seedDeveloperUser();
    }

    /**
     * Bootstraps the platform developer account (no tenant) used to manage the global device
     * inventory. Idempotent and skipped when credentials are not configured. Runs after role
     * seeding so {@code ROLE_DEVELOPER} is guaranteed to exist.
     */
    private void seedDeveloperUser() {
        if (developerEmail == null || developerEmail.isBlank()
                || developerPassword == null || developerPassword.isBlank()) {
            LOGGER.info("Developer credentials not configured; skipping developer user bootstrap");
            return;
        }
        var email = new Email(developerEmail);
        if (userRepository.existsByEmail(email)) {
            LOGGER.info("Developer user already present; skipping bootstrap");
            return;
        }
        var developerRole = roleRepository.findByName(RoleName.ROLE_DEVELOPER)
                .orElseThrow(() -> new IllegalStateException("ROLE_DEVELOPER must be seeded before the developer user"));
        var user = new User(email, new Password(hashingService.encode(developerPassword)), List.of(developerRole));
        userRepository.save(user);
        LOGGER.info("Developer user bootstrapped: {}", developerEmail);
    }

    private Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}
