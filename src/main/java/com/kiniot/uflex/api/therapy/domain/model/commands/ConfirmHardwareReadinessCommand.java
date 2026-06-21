package com.kiniot.uflex.api.therapy.domain.model.commands;

import java.util.UUID;

public record ConfirmHardwareReadinessCommand(
        UUID sessionId,
        Boolean sensorsPlaced
) {}
