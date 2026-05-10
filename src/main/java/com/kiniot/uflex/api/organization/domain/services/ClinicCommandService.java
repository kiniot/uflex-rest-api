package com.kiniot.uflex.api.organization.domain.services;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Clinic;
import com.kiniot.uflex.api.organization.domain.model.commands.ActivateClinicCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterClinicCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.SuspendClinicCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.UpdateClinicContactInfoCommand;

import java.util.Optional;

public interface ClinicCommandService {
    Optional<Clinic> handle(RegisterClinicCommand command);
    void handle(ActivateClinicCommand command);
    void handle(SuspendClinicCommand command);
    void handle(UpdateClinicContactInfoCommand command);
}