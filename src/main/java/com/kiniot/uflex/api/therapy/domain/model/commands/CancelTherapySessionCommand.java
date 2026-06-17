package com.kiniot.uflex.api.therapy.domain.model.commands;

import java.util.UUID;

public record CancelTherapySessionCommand(UUID sessionId, String reason) {}
