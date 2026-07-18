package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

/**
 * A serie together with the repetitions actually recorded against it. Unlike
 * {@link SerieDetailsResource} (prescription only, shared with the mobile app), this carries the
 * execution.
 *
 * <p>{@code durationSeconds}/{@code restDurationSeconds} are prescribed values, not measurements:
 * a serie has no start/end timestamps, so real duration is only inferable from the repetitions'
 * {@code recordedAt}.
 */
@Builder
public record SerieExecutionResource(
        UUID serieId,
        UUID exerciseId,
        Integer targetRepetitions,
        Double targetRom,
        String movementType,
        String bodyPart,
        Integer durationSeconds,
        Integer restDurationSeconds,
        String status,
        List<CompletedRepetitionResource> repetitions
) {}
