package com.kiniot.uflex.api.organization.domain.services;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Patient;
import com.kiniot.uflex.api.organization.domain.model.commands.AssignPatientToPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.AssignTreatmentPlanToPatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.DischargePatientCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientCommand;

import java.util.Optional;

public interface PatientCommandService {
    Optional<Patient> handle(RegisterPatientCommand command);
    void handle(AssignPatientToPhysiotherapistCommand command);
    void handle(AssignTreatmentPlanToPatientCommand command);
    void handle(DischargePatientCommand command);
}