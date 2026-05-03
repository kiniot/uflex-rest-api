package com.kiniot.uflex.api.iam.interfaces.rest.transform;

import com.kiniot.uflex.api.iam.domain.model.aggregates.User;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        var userId = user.getId() != null ? user.getId().id().toString() : null;
        return new AuthenticatedUserResource(user.getId().id().toString(), user.getEmail().email(), token);
    }
}
