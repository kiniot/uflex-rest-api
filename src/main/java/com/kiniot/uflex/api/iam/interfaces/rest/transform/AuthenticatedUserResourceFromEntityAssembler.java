package com.kiniot.uflex.api.iam.interfaces.rest.transform;

import com.kiniot.uflex.api.iam.domain.model.aggregates.User;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        var roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();
        var tenantId = user.getTenantId() != null && user.getTenantId().tenantId() != null
                ? user.getTenantId().tenantId().toString()
                : null;
        return new AuthenticatedUserResource(
                user.getId().id().toString(),
                user.getEmail().email(),
                roles,
                tenantId,
                token);
    }
}
