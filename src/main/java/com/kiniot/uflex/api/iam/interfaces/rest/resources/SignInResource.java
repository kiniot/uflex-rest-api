package com.kiniot.uflex.api.iam.interfaces.rest.resources;

public record SignInResource(
        String email,
        String password
) {
}
