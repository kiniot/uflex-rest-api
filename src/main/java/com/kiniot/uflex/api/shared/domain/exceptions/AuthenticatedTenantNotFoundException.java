package com.kiniot.uflex.api.shared.domain.exceptions;

public class AuthenticatedTenantNotFoundException extends RuntimeException {
    public AuthenticatedTenantNotFoundException() {
        super("Authenticated tenant ID is required but not present");
    }
}
