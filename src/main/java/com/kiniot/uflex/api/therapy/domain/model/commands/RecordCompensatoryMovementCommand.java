package com.kiniot.uflex.api.therapy.domain.model.commands;

import java.util.UUID;

public record RecordCompensatoryMovementCommand(
        UUID sessionId,
        String type,
        UUID edgeSequenceId
) {}
