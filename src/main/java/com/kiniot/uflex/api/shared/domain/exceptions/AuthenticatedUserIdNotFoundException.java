package com.kiniot.uflex.api.shared.domain.exceptions;

public class AuthenticatedUserIdNotFoundException extends RuntimeException {
    public AuthenticatedUserIdNotFoundException() {
        super("Authenticated user ID is required but not present");
    }
}
