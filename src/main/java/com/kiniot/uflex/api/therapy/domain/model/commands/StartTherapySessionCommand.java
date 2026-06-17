package com.kiniot.uflex.api.therapy.domain.model.commands;

import java.util.Objects;
import java.util.UUID;

public record StartTherapySessionCommand(UUID sessionId) {
    public StartTherapySessionCommand {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
    }
}
