package com.kiniot.uflex.api.therapy.domain.model.commands;

import java.util.UUID;

public record ReportPainLevelCommand(UUID sessionId, Integer painLevel) {}
