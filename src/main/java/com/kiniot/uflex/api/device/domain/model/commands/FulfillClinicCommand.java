package com.kiniot.uflex.api.device.domain.model.commands;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;

/**
 * On-demand fulfillment: assign available stock to a clinic to cover the shortfall between
 * the kits it paid for and the kits it owns. The requested amount is resolved from the
 * clinic's current subscription; assignment is capped by available stock.
 */
public record FulfillClinicCommand(ClinicId clinicId) {
    public FulfillClinicCommand {
        if (clinicId == null || clinicId.id() == null) {
            throw new IllegalArgumentException("Clinic ID cannot be null");
        }
    }
}
