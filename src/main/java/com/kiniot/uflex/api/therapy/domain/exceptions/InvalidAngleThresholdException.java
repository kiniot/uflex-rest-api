package com.kiniot.uflex.api.therapy.domain.exceptions;

public class InvalidAngleThresholdException extends RuntimeException {

    private InvalidAngleThresholdException(String message) {
        super(message);
    }

    public static InvalidAngleThresholdException forAngle(Double achievedAngle, Double minAngle, Double maxAngle) {
        return new InvalidAngleThresholdException(
                "Achieved angle %.1f is outside the valid threshold [%.1f, %.1f]"
                        .formatted(achievedAngle, minAngle, maxAngle));
    }
}