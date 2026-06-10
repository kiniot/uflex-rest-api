package com.kiniot.uflex.api.therapy.domain.model.commands;

import java.util.UUID;

public record InitiateTherapyPreparationCommand(
        UUID patientId,
        UUID treatmentPlanId,
        String iotDeviceId,
        UUID planningRoutineId
) {}
