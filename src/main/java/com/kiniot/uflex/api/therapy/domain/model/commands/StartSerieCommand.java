package com.kiniot.uflex.api.therapy.domain.model.commands;

import java.util.UUID;

public record StartSerieCommand(UUID sessionId, UUID serieId) {}
