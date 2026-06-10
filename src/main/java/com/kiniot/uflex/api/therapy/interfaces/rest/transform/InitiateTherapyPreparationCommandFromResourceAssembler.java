package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.commands.InitiateTherapyPreparationCommand;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.InitiateTherapyPreparationResource;

public final class InitiateTherapyPreparationCommandFromResourceAssembler {

    private InitiateTherapyPreparationCommandFromResourceAssembler() {}

    public static InitiateTherapyPreparationCommand toCommandFromResource(InitiateTherapyPreparationResource resource) {
        return new InitiateTherapyPreparationCommand(
                resource.patientId(),
                resource.treatmentPlanId(),
                resource.iotDeviceId(),
                resource.routineId()
        );
    }
}
