package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.commands.RecordCompensatoryMovementCommand;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.RecordCompensatoryMovementResource;

import java.util.UUID;

public final class RecordCompensatoryMovementCommandFromResourceAssembler {

    private RecordCompensatoryMovementCommandFromResourceAssembler() {}

    public static RecordCompensatoryMovementCommand toCommandFromResource(
            UUID sessionId, UUID edgeSequenceId, RecordCompensatoryMovementResource resource) {
        return new RecordCompensatoryMovementCommand(sessionId, resource.type(), edgeSequenceId);
    }
}
