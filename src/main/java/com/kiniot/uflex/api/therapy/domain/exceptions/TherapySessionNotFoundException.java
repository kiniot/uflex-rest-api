package com.kiniot.uflex.api.therapy.domain.exceptions;

public class TherapySessionNotFoundException extends RuntimeException {

    private TherapySessionNotFoundException(String identifier) {
        super("TherapySession with ID %s not found".formatted(identifier));
    }

    public static TherapySessionNotFoundException withId(String id) {
        return new TherapySessionNotFoundException(id);
    }
}
