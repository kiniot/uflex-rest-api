package com.kiniot.uflex.api.therapy.domain.exceptions;

public class SerieNotFoundException extends RuntimeException {

    private SerieNotFoundException(String identifier) {
        super("Serie with ID %s not found in the active routine".formatted(identifier));
    }

    public static SerieNotFoundException withId(String id) {
        return new SerieNotFoundException(id);
    }
}
