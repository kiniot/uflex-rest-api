package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.commands.RecordAnomalousMovementCommand;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.RecordAnomalousMovementResource;

import java.util.UUID;

public final class RecordAnomalousMovementCommandFromResourceAssembler {

    private RecordAnomalousMovementCommandFromResourceAssembler() {}

    public static RecordAnomalousMovementCommand toCommandFromResource(
            UUID sessionId, UUID edgeSequenceId, RecordAnomalousMovementResource resource) {
        return new RecordAnomalousMovementCommand(sessionId, resource.alertType(), edgeSequenceId);
    }
}
