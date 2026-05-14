package com.kiniot.uflex.api.iam.interfaces.rest.resources;

import java.util.List;

public record AuthenticatedUserResource(
        String id,
        String email,
        List<String> roles,
        String tenantId,
        String token
) {
}
