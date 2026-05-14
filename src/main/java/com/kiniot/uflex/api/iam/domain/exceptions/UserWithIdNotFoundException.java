package com.kiniot.uflex.api.iam.domain.exceptions;

public class UserWithIdNotFoundException extends RuntimeException {
    public UserWithIdNotFoundException(String userId) {
        super("User with ID %s not found".formatted(userId));
    }
}
