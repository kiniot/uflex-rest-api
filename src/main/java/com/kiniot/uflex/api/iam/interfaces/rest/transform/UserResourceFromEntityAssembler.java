package com.kiniot.uflex.api.iam.interfaces.rest.transform;

import com.kiniot.uflex.api.iam.domain.model.aggregates.User;
import com.kiniot.uflex.api.iam.domain.model.entities.Role;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.UserResource;

public class UserResourceFromEntityAssembler {
    public static UserResource toResourceFromEntity(User entity) {
        var roles = entity.getRoles().stream().map(Role::getStringName).toList();
        var userId = entity.getId() != null ? entity.getId().id().toString() : null;
        var tenantId = entity.getTenantId() != null && entity.getTenantId().tenantId() != null
                ? entity.getTenantId().tenantId().toString()
                : null;
        return new UserResource(userId, entity.getEmail().email(), roles, tenantId);
    }
}
