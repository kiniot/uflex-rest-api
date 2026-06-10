package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.commands.ConfirmHardwareReadinessCommand;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.ConfirmHardwareReadinessResource;

import java.util.UUID;

public final class ConfirmHardwareReadinessCommandFromResourceAssembler {

    private ConfirmHardwareReadinessCommandFromResourceAssembler() {}

    public static ConfirmHardwareReadinessCommand toCommandFromResource(UUID sessionId, ConfirmHardwareReadinessResource resource) {
        return new ConfirmHardwareReadinessCommand(sessionId, resource.deviceId(), resource.sensorsPlaced());
    }
}
