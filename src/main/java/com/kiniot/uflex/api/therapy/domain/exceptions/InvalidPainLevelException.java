package com.kiniot.uflex.api.therapy.domain.exceptions;

public class InvalidPainLevelException extends RuntimeException {

    private InvalidPainLevelException(String message) {
        super(message);
    }

    public static InvalidPainLevelException forValue(Integer value) {
        return new InvalidPainLevelException(
                "Pain level %d is invalid: must be between 0 and 10".formatted(value));
    }
}
