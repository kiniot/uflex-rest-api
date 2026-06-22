package com.kiniot.uflex.api.device.domain.model.commands;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;

/**
 * Assigns up to {@code requestedTotalKits} stock devices to a clinic. The command service
 * only assigns the shortfall (requested minus devices the clinic already owns) and caps it
 * to whatever stock is available, so it is safe to replay on subscription re-activation.
 */
public record AssignStockToClinicCommand(
        ClinicId clinicId,
        int requestedTotalKits
) {
    public AssignStockToClinicCommand {
        if (clinicId == null || clinicId.id() == null) {
            throw new IllegalArgumentException("Clinic ID cannot be null");
        }
        if (requestedTotalKits < 0) {
            throw new IllegalArgumentException("Requested total kits cannot be negative");
        }
    }
}
