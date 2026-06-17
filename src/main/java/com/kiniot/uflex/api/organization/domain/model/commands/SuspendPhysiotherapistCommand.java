package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;

public record SuspendPhysiotherapistCommand(
        PhysiotherapistId physiotherapistId
) {
    public SuspendPhysiotherapistCommand {
        if (physiotherapistId == null) {
            throw new IllegalArgumentException("Physiotherapist ID cannot be null");
        }
    }
}
