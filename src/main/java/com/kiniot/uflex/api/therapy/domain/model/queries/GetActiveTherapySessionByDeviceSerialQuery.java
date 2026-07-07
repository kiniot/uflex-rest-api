package com.kiniot.uflex.api.therapy.domain.model.queries;

/**
 * Resolves the active (Pending/Ready/InProgress) therapy session for a physical device,
 * identified by its kit serial number (kitSerial). This is the device-side counterpart to
 * {@link GetActiveTherapySessionByPatientIdQuery}, needed to correlate edge-forwarded
 * measurements back to the session that produced them (see device-identity-contract).
 *
 * @param deviceSerial the kit serial number; equals {@code Device.serialNumber} and the edge
 *                     {@code device_id}.
 */
public record GetActiveTherapySessionByDeviceSerialQuery(String deviceSerial) {
    public GetActiveTherapySessionByDeviceSerialQuery {
        if (deviceSerial == null || deviceSerial.isBlank()) {
            throw new IllegalArgumentException("Device serial cannot be null or blank");
        }
    }
}
