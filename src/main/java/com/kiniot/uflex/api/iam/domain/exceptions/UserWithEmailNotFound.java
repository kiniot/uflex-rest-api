package com.kiniot.uflex.api.iam.domain.exceptions;

public class UserWithEmailNotFound extends RuntimeException {
    public UserWithEmailNotFound(String email) {
        super("User with email %s not found".formatted(email));
    }
}
