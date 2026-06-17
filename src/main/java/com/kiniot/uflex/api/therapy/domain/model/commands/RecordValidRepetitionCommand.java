package com.kiniot.uflex.api.therapy.domain.model.commands;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecordValidRepetitionCommand(
        UUID sessionId,
        UUID serieId,
        Double achievedAngle,
        LocalDateTime recordedAt,
        UUID edgeSequenceId
) {}
