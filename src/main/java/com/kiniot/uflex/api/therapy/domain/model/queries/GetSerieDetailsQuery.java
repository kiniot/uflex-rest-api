package com.kiniot.uflex.api.therapy.domain.model.queries;

import java.util.UUID;

public record GetSerieDetailsQuery(UUID sessionId, UUID serieId) {}
