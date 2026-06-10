package com.kiniot.uflex.api.therapy.domain.model.commands;

import java.util.Objects;
import java.util.UUID;

public record FinalizeTherapySessionCommand(UUID sessionId) {
    public FinalizeTherapySessionCommand {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
    }
}
