package com.kiniot.uflex.api.shared.interfaces.rest;

import com.kiniot.uflex.api.iam.domain.exceptions.UserWithIdNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.CrossClinicAssignmentException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiErrorCodeResolverTests {

    private final ApiErrorCodeResolver resolver = new ApiErrorCodeResolver();

    @Test
    void shouldResolveCustomExceptionNameToUpperSnakeCase() {
        String code = resolver.resolve(new UserWithIdNotFoundException("123"));

        assertEquals("USER_WITH_ID_NOT_FOUND", code);
    }

    @Test
    void shouldResolveCustomExceptionWithoutSuffixToUpperSnakeCase() {
        String code = resolver.resolve(new CrossClinicAssignmentException());

        assertEquals("CROSS_CLINIC_ASSIGNMENT", code);
    }

    @Test
    void shouldResolveIllegalArgumentExceptionToBadRequest() {
        String code = resolver.resolve(new IllegalArgumentException("Invalid"));

        assertEquals("BAD_REQUEST", code);
    }

    @Test
    void shouldResolveIllegalStateExceptionToConflict() {
        String code = resolver.resolve(new IllegalStateException("Conflict"));

        assertEquals("CONFLICT", code);
    }

    @Test
    void shouldResolveAccessDeniedExceptionToAccessDenied() {
        String code = resolver.resolve(new AccessDeniedException("Forbidden"));

        assertEquals("ACCESS_DENIED", code);
    }

    @Test
    void shouldResolveAuthenticationExceptionToAuthRequired() {
        String code = resolver.resolve(new BadCredentialsException("Invalid token"));

        assertEquals("AUTH_REQUIRED", code);
    }

    @Test
    void shouldResolveErrorResponseExceptionFromStatus() {
        String code = resolver.resolve(new ResponseStatusException(HttpStatus.NOT_FOUND, "Missing"));

        assertEquals("NOT_FOUND", code);
    }

    @Test
    void shouldFallbackToInternalServerError() {
        String code = resolver.resolve(new RuntimeException("Unexpected"));

        assertEquals("INTERNAL_SERVER_ERROR", code);
    }
}
