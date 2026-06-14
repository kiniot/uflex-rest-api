package com.kiniot.uflex.api.iam.application.internal.queryservices;

import com.kiniot.uflex.api.iam.application.internal.outboundservices.identity.IdentityService;
import com.kiniot.uflex.api.iam.domain.model.aggregates.User;
import com.kiniot.uflex.api.iam.domain.model.queries.*;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.TenantId;
import com.kiniot.uflex.api.iam.domain.services.UserQueryService;
import com.kiniot.uflex.api.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedTenantNotFoundException;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserIdNotFoundException;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;
    private final IdentityService identityService;

    public UserQueryServiceImpl(
            UserRepository userRepository,
            IdentityService identityService
    ) {
        this.userRepository = userRepository;
        this.identityService = identityService;
    }

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findByIdWithRoles(query.userId());
    }

    @Override
    public Optional<User> handle(GetUserByEmailQuery query) {
        return userRepository.findByEmailWithRoles(query.email());
    }

    @Override
    public Optional<UserId> handle(GetContextUserIdQuery query) {
        return identityService.getUserId()
                .map(UUID::fromString)
                .map(UserId::new);
    }

    @Override
    public Optional<TenantId> handle(GetContextTenantIdQuery query) {
        return Optional.of(identityService.getTenantId()
                .map(UUID::fromString)
                .map(TenantId::new)
                .orElseThrow(AuthenticatedTenantNotFoundException::new));
    }

    @Override
    public Optional<UserId> handle(GetCurrentUserIdQuery query) {
        var contextUserId = identityService.getUserId()
                .map(UUID::fromString)
                .map(UserId::new)
                .orElseThrow(AuthenticatedUserIdNotFoundException::new);
        return userRepository.findById(contextUserId)
                .map(User::getId);
    }

    @Override
    public Optional<TenantId> handle(GetCurrentTenantIdQuery query) {
        var contextUserId = identityService.getUserId()
                .map(UUID::fromString)
                .map(UserId::new)
                .orElseThrow(AuthenticatedUserIdNotFoundException::new);
        return userRepository.findById(contextUserId)
                .map(User::getTenantId)
                .filter(TenantId::isAssigned);
    }
}
