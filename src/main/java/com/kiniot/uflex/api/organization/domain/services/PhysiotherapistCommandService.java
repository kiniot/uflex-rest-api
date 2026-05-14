package com.kiniot.uflex.api.organization.domain.services;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Physiotherapist;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPhysiotherapistCommand;

import java.util.Optional;

public interface PhysiotherapistCommandService {
    Optional<Physiotherapist> handle(RegisterPhysiotherapistCommand command);
}