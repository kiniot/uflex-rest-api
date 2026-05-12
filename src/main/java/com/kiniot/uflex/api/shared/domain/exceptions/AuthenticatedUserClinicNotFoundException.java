package com.kiniot.uflex.api.shared.domain.exceptions;

public class AuthenticatedUserClinicNotFoundException extends RuntimeException {
    public AuthenticatedUserClinicNotFoundException() {
        super("Authenticated user is not associated with any clinic");
    }
}
