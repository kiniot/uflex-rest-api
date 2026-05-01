package com.kiniot.uflex.api.iam.application.internal.commandservices;

import com.kiniot.uflex.api.iam.application.internal.outboundservices.hashing.HashingService;
import com.kiniot.uflex.api.iam.application.internal.outboundservices.tokens.TokenService;
import com.kiniot.uflex.api.iam.domain.model.aggregates.User;
import com.kiniot.uflex.api.iam.domain.model.commands.SignInCommand;
import com.kiniot.uflex.api.iam.domain.model.commands.SignUpCommand;
import com.kiniot.uflex.api.iam.domain.model.entities.Role;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Password;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.RoleName;
import com.kiniot.uflex.api.iam.domain.services.UserCommandService;
import com.kiniot.uflex.api.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.kiniot.uflex.api.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;

    public UserCommandServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            HashingService hashingService,
            TokenService tokenService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByEmail(command.email()))
            throw new IllegalArgumentException("Email already in use");
        var roleNames = (command.roles() == null || command.roles().isEmpty())
                ? List.of(RoleName.ROLE_USER)
                : command.roles().stream().map(Role::getName).toList();
        var roles = roleRepository.findAllByNameIn(roleNames);
        if (roles.size() != roleNames.size())
            throw new RuntimeException("One or more roles not found in database");
        var user = new User(command.email(), new Password(hashingService.encode(command.password().password())), roles);
        user.registerUserCreatedEvent();
        userRepository.save(user);
        return Optional.of(user);
    }

    @Override
    @Transactional
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        var user = userRepository.findByEmailWithRoles(command.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!hashingService.matches(command.password().password(), user.getPassword().password()))
            throw new IllegalArgumentException("Invalid password");
        var roles = user.getRoles().stream().map(role -> role.getName().name()).toList();
        var token = tokenService.generateToken(Objects.requireNonNull(user.getId()).id().toString(), user.getEmail().email(), roles);
        return Optional.of(ImmutablePair.of(user, token));
    }
}