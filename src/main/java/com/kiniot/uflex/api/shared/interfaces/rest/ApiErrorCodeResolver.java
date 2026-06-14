package com.kiniot.uflex.api.shared.interfaces.rest;

import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponseException;

import java.util.Locale;

@Component
public class ApiErrorCodeResolver {

    public String resolve(Throwable throwable) {
        if (throwable == null) {
            return "INTERNAL_SERVER_ERROR";
        }
        if (throwable instanceof AuthenticationException) {
            return "AUTH_REQUIRED";
        }
        if (throwable instanceof AccessDeniedException) {
            return "ACCESS_DENIED";
        }
        if (throwable instanceof IllegalArgumentException) {
            return "BAD_REQUEST";
        }
        if (throwable instanceof IllegalStateException) {
            return "CONFLICT";
        }
        if (throwable instanceof ErrorResponseException errorResponseException) {
            return resolveFromStatus(errorResponseException.getStatusCode());
        }
        Package exceptionPackage = throwable.getClass().getPackage();
        if (exceptionPackage != null && exceptionPackage.getName().startsWith("com.kiniot.uflex.api")) {
            return toUpperSnakeCase(removeExceptionSuffix(throwable.getClass().getSimpleName()));
        }
        return "INTERNAL_SERVER_ERROR";
    }

    private String resolveFromStatus(HttpStatusCode statusCode) {
        return switch (statusCode.value()) {
            case 400 -> "BAD_REQUEST";
            case 401 -> "AUTH_REQUIRED";
            case 403 -> "ACCESS_DENIED";
            case 404 -> "NOT_FOUND";
            case 409 -> "CONFLICT";
            default -> "HTTP_" + statusCode.value();
        };
    }

    private String removeExceptionSuffix(String simpleClassName) {
        if (simpleClassName.endsWith("Exception")) {
            return simpleClassName.substring(0, simpleClassName.length() - "Exception".length());
        }
        return simpleClassName;
    }

    private String toUpperSnakeCase(String value) {
        String snakeCase = value
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2");
        return snakeCase.toUpperCase(Locale.ROOT);
    }
}
