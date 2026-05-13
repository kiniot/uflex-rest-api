package com.kiniot.uflex.api.iam.application.internal.commandservices;

import com.kiniot.uflex.api.iam.application.internal.outboundservices.hashing.HashingService;
import com.kiniot.uflex.api.iam.application.internal.outboundservices.identity.IdentityService;
import com.kiniot.uflex.api.iam.application.internal.outboundservices.tokens.TokenService;
import com.kiniot.uflex.api.iam.domain.exceptions.UserWithEmailNotFound;
import com.kiniot.uflex.api.iam.domain.exceptions.UserWithIdNotFoundException;
import com.kiniot.uflex.api.iam.domain.model.aggregates.User;
import com.kiniot.uflex.api.iam.domain.model.commands.*;
import com.kiniot.uflex.api.iam.domain.model.entities.Role;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Password;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.RoleName;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.TenantId;
import com.kiniot.uflex.api.iam.domain.services.UserCommandService;
import com.kiniot.uflex.api.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.kiniot.uflex.api.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final IdentityService identityService;

    public UserCommandServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            HashingService hashingService,
            TokenService tokenService,
            IdentityService identityService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.identityService = identityService;
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
                .orElseThrow(() -> new UserWithEmailNotFound(command.email().email()));
        if (!hashingService.matches(command.password().password(), user.getPassword().password()))
            throw new IllegalArgumentException("Invalid password");
        var roles = user.getRoles().stream().map(role -> role.getName().name()).toList();
        var tenantId = user.getTenantId() != null && user.getTenantId().tenantId() != null
                ? user.getTenantId().tenantId().toString()
                : null;
        var token = tokenService.generateToken(Objects.requireNonNull(user.getId()).id().toString(), user.getEmail().email(), roles, tenantId);
        return Optional.of(ImmutablePair.of(user, token));
    }

    @Override
    @Transactional
    public Optional<User> handle(SignUpVerifiedUserCommand command) {
        if (userRepository.existsByEmail(command.emailAddress()))
            throw new RuntimeException("Username already exists");
        var roles = (command.roles() == null || command.roles().isEmpty())
                ? List.of(roleRepository.findByName(RoleName.ROLE_USER).orElseThrow(() -> new RuntimeException("Default role not found")))
                : command.roles().stream()
                  .map(role -> roleRepository.findByName(role.getName()).orElseThrow(() -> new RuntimeException("Role name not found")))
                  .toList();
        var tenantId = new TenantId(UUID.fromString(identityService.getTenantId()
                .orElseThrow(() -> new RuntimeException("Tenant ID not found in identity service"))));
        var user = new User(
                command.emailAddress(),
                new Password(hashingService.encode(command.password())),
                roles,
                tenantId
        );
        user.notifySignedUpAndActivated(command.emailAddress().email(), command.password());
        userRepository.save(user);
        return userRepository.findByEmail(command.emailAddress());
    }

    @Override
    @Transactional
    public void handle(ChangePasswordCommand command) {
        var user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserWithIdNotFoundException(command.userId().toString()));
        if (!hashingService.matches(command.currentPassword().password(), user.getPassword().password()))
            throw new IllegalArgumentException("Current password is incorrect");
        user.changePassword(new Password(hashingService.encode(command.newPassword().password())));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void handle(AssignUserTenantId command) {
        var user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserWithIdNotFoundException(command.userId().toString()));
        try {
            user.associateTenant(command.tenantId());
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign tenant to user: %s".formatted(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public void handle(AssignUserRoleCommand command) {
        var user = userRepository.findByIdWithRoles(command.userId())
                .orElseThrow(() -> new UserWithIdNotFoundException(command.userId().toString()));
        var role = roleRepository.findByName(command.roleName())
                .orElseThrow(() -> new IllegalArgumentException("Role not found: %s".formatted(command.roleName().name())));
        var alreadyAssigned = user.getRoles().stream()
                .anyMatch(userRole -> userRole.getName().equals(command.roleName()));
        if (alreadyAssigned) {
            return;
        }
        user.addRole(role);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void handle(DeleteUserCommand command) {
        if (!userRepository.existsById(command.userId())) {
            return;
        }
        userRepository.deleteById(command.userId());
    }
}
