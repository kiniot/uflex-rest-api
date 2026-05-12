package com.kiniot.uflex.api.planning.domain.exceptions;

public class ExerciseClinicMismatchException extends RuntimeException {
    public ExerciseClinicMismatchException(String exerciseId, String clinicId) {
        super("Exercise with ID %s does not belong to clinic %s".formatted(exerciseId, clinicId));
    }
}
