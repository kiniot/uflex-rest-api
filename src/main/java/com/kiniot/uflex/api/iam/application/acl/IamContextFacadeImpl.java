package com.kiniot.uflex.api.iam.application.acl;

import com.kiniot.uflex.api.iam.domain.model.queries.GetAuthenticatedUserIdQuery;
import com.kiniot.uflex.api.iam.domain.model.queries.GetAuthenticatedUserTenantIdQuery;
import com.kiniot.uflex.api.iam.domain.model.queries.GetUserByIdQuery;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.iam.domain.services.UserQueryService;
import com.kiniot.uflex.api.iam.interfaces.acl.IamContextFacade;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IamContextFacadeImpl implements IamContextFacade {

    private final UserQueryService userQueryService;

    public IamContextFacadeImpl(
            UserQueryService userQueryService
    ) {
        this.userQueryService = userQueryService;
    }

    @Override
    public String fetchUserEmailAddressByUserId(String userId) {
        var getUserByIdQuery = new GetUserByIdQuery(new UserId(UUID.fromString(userId)));
        var user = userQueryService.handle(getUserByIdQuery);
        return user.map(u -> u.getEmail().email()).orElse("");
    }

    @Override
    public String fetchAuthenticatedUserId() {
        var getAuthenticatedUserIdQuery = new GetAuthenticatedUserIdQuery();
        return userQueryService.handle(getAuthenticatedUserIdQuery)
                .map(userId -> userId.id().toString())
                .orElseThrow(() -> new IllegalStateException("User fetch authenticated failed"));
    }

    @Override
    public String fetchAuthenticatedUserTenantId() {
        var getAuthenticatedUserTenantIdQuery = new GetAuthenticatedUserTenantIdQuery();
        var tenantId = userQueryService.handle(getAuthenticatedUserTenantIdQuery);
        return tenantId.isEmpty() ? "" : tenantId.get().tenantId().toString();
    }
}