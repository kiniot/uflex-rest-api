package com.kiniot.uflex.api.therapy.domain.model.commands;

import java.util.UUID;

/**
 * Initiates therapy preparation for a patient.
 *
 * @param iotDeviceId the kit serial number (kitSerial) of the measuring device. By contract this
 *                    equals {@code Device.serialNumber} and the edge {@code device_id}; it is not
 *                    the backend Device UUID nor a MAC address (see device-identity-contract).
 */
public record InitiateTherapyPreparationCommand(
        UUID patientId,
        UUID treatmentPlanId,
        String iotDeviceId,
        UUID planningRoutineId
) {
    public InitiateTherapyPreparationCommand {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
        if (treatmentPlanId == null) {
            throw new IllegalArgumentException("Treatment plan ID cannot be null");
        }
        if (iotDeviceId == null || iotDeviceId.isBlank()) {
            throw new IllegalArgumentException("IoT device id (kit serial) cannot be null or blank");
        }
        if (planningRoutineId == null) {
            throw new IllegalArgumentException("Planning routine ID cannot be null");
        }
    }
}
