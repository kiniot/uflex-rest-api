package com.kiniot.uflex.api.organization.domain.services;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Patient;
import com.kiniot.uflex.api.organization.domain.model.commands.AssignPatientToPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.CompletePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.DischargePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.MarkPatientInactiveCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.ReactivatePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientByClinicAdminCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientByPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.UpdateCurrentPatientProfileCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.UpdatePatientByClinicAdminCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.UpdatePatientByPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.DeletePatientCommand;

import java.util.Optional;

public interface PatientCommandService {
    Optional<Patient> handle(RegisterPatientByClinicAdminCommand command);
    Optional<Patient> handle(RegisterPatientByPhysiotherapistCommand command);
    Optional<Patient> handle(UpdatePatientByClinicAdminCommand command);
    Optional<Patient> handle(UpdatePatientByPhysiotherapistCommand command);
    Optional<Patient> handle(UpdateCurrentPatientProfileCommand command);
    void handle(AssignPatientToPhysiotherapistCommand command);
    void handle(CompletePatientCommand command);
    void handle(MarkPatientInactiveCommand command);
    void handle(ReactivatePatientCommand command);
    void handle(DischargePatientCommand command);
    void handle(DeletePatientCommand command);
}
