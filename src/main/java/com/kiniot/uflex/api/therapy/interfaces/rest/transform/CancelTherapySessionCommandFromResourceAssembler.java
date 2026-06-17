package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.commands.CancelTherapySessionCommand;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.CancelTherapySessionResource;

import java.util.UUID;

public final class CancelTherapySessionCommandFromResourceAssembler {

    private CancelTherapySessionCommandFromResourceAssembler() {}

    public static CancelTherapySessionCommand toCommandFromResource(UUID sessionId, CancelTherapySessionResource resource) {
        return new CancelTherapySessionCommand(sessionId, resource.reason());
    }
}
