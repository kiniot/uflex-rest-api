package com.kiniot.uflex.api.therapy.domain.model.commands;

import java.time.Instant;
import java.util.UUID;

public record RecordValidRepetitionCommand(
        UUID sessionId,
        UUID serieId,
        Double achievedAngle,
        Instant recordedAt,
        UUID edgeSequenceId
) {}
