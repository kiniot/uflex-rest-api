package com.kiniot.uflex.api.iam.domain.exceptions;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String roleName) {
        super("Role not found: %s".formatted(roleName));
    }
}
