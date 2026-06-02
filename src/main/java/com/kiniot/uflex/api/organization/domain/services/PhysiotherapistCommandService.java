package com.kiniot.uflex.api.organization.domain.services;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Physiotherapist;
import com.kiniot.uflex.api.organization.domain.model.commands.ReactivatePhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.SuspendPhysiotherapistCommand;

import java.util.Optional;

public interface PhysiotherapistCommandService {
    Optional<Physiotherapist> handle(RegisterPhysiotherapistCommand command);
    void handle(SuspendPhysiotherapistCommand command);
    void handle(ReactivatePhysiotherapistCommand command);
}
