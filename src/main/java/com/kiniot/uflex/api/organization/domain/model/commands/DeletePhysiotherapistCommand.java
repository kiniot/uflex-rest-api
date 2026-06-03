package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;

public record DeletePhysiotherapistCommand(
        PhysiotherapistId physiotherapistId
) {}
