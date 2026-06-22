package com.kiniot.uflex.api.therapy.domain.model.commands;

import com.kiniot.uflex.api.therapy.domain.model.valueobjects.RepetitionClassification;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecordValidRepetitionCommand(
        UUID sessionId,
        UUID serieId,
        Double peakAngle,
        Double achievedRom,
        RepetitionClassification classification,
        LocalDateTime recordedAt,
        UUID edgeSequenceId
) {}
