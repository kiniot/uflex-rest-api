package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;

public record ReactivatePhysiotherapistCommand(
        PhysiotherapistId physiotherapistId
) {
    public ReactivatePhysiotherapistCommand {
        if (physiotherapistId == null) {
            throw new IllegalArgumentException("Physiotherapist ID cannot be null");
        }
    }
}
