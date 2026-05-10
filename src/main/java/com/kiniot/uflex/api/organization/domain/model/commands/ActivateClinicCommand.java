package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;

public record ActivateClinicCommand(
        ClinicId clinicId
) {
    public ActivateClinicCommand {
        if (clinicId == null) {
            throw new IllegalArgumentException("Clinic ID cannot be null");
        }
    }
}