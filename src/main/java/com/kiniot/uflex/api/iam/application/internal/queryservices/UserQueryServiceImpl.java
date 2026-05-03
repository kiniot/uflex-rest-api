package com.kiniot.uflex.api.iam.application.internal.queryservices;

import com.kiniot.uflex.api.iam.application.internal.outboundservices.identity.IdentityService;
import com.kiniot.uflex.api.iam.domain.model.aggregates.User;
import com.kiniot.uflex.api.iam.domain.model.queries.GetAuthenticatedUserIdQuery;
import com.kiniot.uflex.api.iam.domain.model.queries.GetAuthenticatedUserTenantIdQuery;
import com.kiniot.uflex.api.iam.domain.model.queries.GetUserByEmailQuery;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.TenantId;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.iam.domain.services.UserQueryService;
import com.kiniot.uflex.api.iam.infrastructure.persistence.jpa.repositories.UserRepository;
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
    public Optional<User> handle(GetUserByEmailQuery query) {
        return userRepository.findByEmailWithRoles(query.email());
    }

    @Override
    public Optional<UserId> handle(GetAuthenticatedUserIdQuery query) {
        return identityService.getUserId()
                .map(UUID::fromString)
                .map(UserId::new);
    }

    @Override
    public Optional<TenantId> handle(GetAuthenticatedUserTenantIdQuery query) {
        return Optional.of(identityService.getTenantId()
                .map(UUID::fromString)
                .map(TenantId::new)
                .orElseThrow(() -> new IllegalStateException("Tenant ID is required but not present")));
    }
}