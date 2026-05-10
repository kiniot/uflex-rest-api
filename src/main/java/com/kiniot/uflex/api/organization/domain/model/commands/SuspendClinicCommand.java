package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;

public record SuspendClinicCommand(
        ClinicId clinicId,
        String reason
) {
    public SuspendClinicCommand {
        if (clinicId == null) {
            throw new IllegalArgumentException("Clinic ID cannot be null");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be null or blank");
        }
    }
}