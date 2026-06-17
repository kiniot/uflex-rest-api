package com.kiniot.uflex.api.organization.domain.services;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Physiotherapist;
import com.kiniot.uflex.api.organization.domain.model.commands.DeletePhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.ReactivatePhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.SuspendPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.UpdatePhysiotherapistCommand;

import java.util.Optional;

public interface PhysiotherapistCommandService {
    Optional<Physiotherapist> handle(RegisterPhysiotherapistCommand command);
    Optional<Physiotherapist> handle(UpdatePhysiotherapistCommand command);
    void handle(SuspendPhysiotherapistCommand command);
    void handle(ReactivatePhysiotherapistCommand command);
    void handle(DeletePhysiotherapistCommand command);
}
