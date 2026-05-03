package com.kiniot.uflex.api.iam.interfaces.rest.resources;

public record AuthenticatedUserResource(
        String id,
        String email,
        String token
) {
}
