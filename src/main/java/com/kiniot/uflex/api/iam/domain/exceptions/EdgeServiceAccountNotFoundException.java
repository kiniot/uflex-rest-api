package com.kiniot.uflex.api.iam.domain.exceptions;

public class EdgeServiceAccountNotFoundException extends RuntimeException {
    public EdgeServiceAccountNotFoundException(String id) {
        super("Edge service account not found: " + id);
    }
}
