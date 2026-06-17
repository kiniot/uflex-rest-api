package com.kiniot.uflex.api.shared.interfaces.rest;

import com.kiniot.uflex.api.shared.interfaces.rest.resources.ErrorResource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class ErrorResponseFactory {

    private final ApiErrorCodeResolver apiErrorCodeResolver;

    public ErrorResponseFactory(ApiErrorCodeResolver apiErrorCodeResolver) {
        this.apiErrorCodeResolver = apiErrorCodeResolver;
    }

    public ResponseEntity<ErrorResource> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Throwable throwable
    ) {
        return ResponseEntity.status(status).body(build(status, message, request, throwable));
    }

    public ErrorResource build(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Throwable throwable
    ) {
        return new ErrorResource(
                apiErrorCodeResolver.resolve(throwable),
                message,
                status.value(),
                status.getReasonPhrase(),
                OffsetDateTime.now().toString(),
                request.getRequestURI()
        );
    }
}
