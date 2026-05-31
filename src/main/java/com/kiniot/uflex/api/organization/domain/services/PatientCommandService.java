package com.kiniot.uflex.api.organization.domain.services;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Patient;
import com.kiniot.uflex.api.organization.domain.model.commands.AssignPatientToPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.DischargePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientByClinicAdminCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientByPhysiotherapistCommand;

import java.util.Optional;

public interface PatientCommandService {
    Optional<Patient> handle(RegisterPatientByClinicAdminCommand command);
    Optional<Patient> handle(RegisterPatientByPhysiotherapistCommand command);
    void handle(AssignPatientToPhysiotherapistCommand command);
    void handle(DischargePatientCommand command);
}
