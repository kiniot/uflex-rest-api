package com.kiniot.uflex.api.organization.domain.services;

import com.kiniot.uflex.api.organization.domain.model.entities.ClinicAdmin;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterClinicAdminCommand;

import java.util.Optional;

public interface ClinicAdminCommandService {
    Optional<ClinicAdmin> handle(RegisterClinicAdminCommand command);
}