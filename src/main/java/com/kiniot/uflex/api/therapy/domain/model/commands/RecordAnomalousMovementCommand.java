package com.kiniot.uflex.api.therapy.domain.model.commands;

import java.util.UUID;

public record RecordAnomalousMovementCommand(
        UUID sessionId,
        String alertType,
        UUID edgeSequenceId
) {}
