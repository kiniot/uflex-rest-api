package com.kiniot.uflex.api.planning.domain.model.valueobjects;

import jakarta.persistence.*;

@Embeddable
public record ExerciseSeries(
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "series_order", nullable = false))
    ExerciseSeriesOrder order,
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "exercise_id", nullable = false, columnDefinition = "UUID"))
    ExerciseId exerciseId,
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    MovementType movementType,
    @Embedded
    @AttributeOverride(name = "degrees", column = @Column(name = "range_of_motion", nullable = false))
    RangeOfMotion rangeOfMotion,
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "repetitions", nullable = false))
    RepetitionCount repetitions,
    @Embedded
    ExerciseDuration duration
) {
    public ExerciseSeries {
        if (order == null) {
            throw new IllegalArgumentException("Exercise series order cannot be null");
        }
        if (exerciseId == null) {
            throw new IllegalArgumentException("Exercise Id cannot be null");
        }
        if (movementType == null) {
            throw new IllegalArgumentException("Movement type cannot be null");
        }
        if (rangeOfMotion == null) {
            throw new IllegalArgumentException("Range of motion cannot be null");
        }
        if (repetitions == null) {
            throw new IllegalArgumentException("Repetitions cannot be null");
        }
        if (duration == null) {
            throw new IllegalArgumentException("Duration cannot be null");
        }
    }
}
