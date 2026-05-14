package com.kiniot.uflex.api.iam.interfaces.rest.resources;

public record ChangePasswordResource(
        String currentPassword,
        String newPassword
) {
}
