package com.kiniot.uflex.api.organization.interfaces.rest.transform;

import com.kiniot.uflex.api.organization.domain.model.commands.UpdateCurrentPatientProfileCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.UpdatePatientByClinicAdminCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.UpdatePatientByPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.BirthDate;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.Dni;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.FirstName;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.Gender;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.LastName;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.MedicalCondition;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhoneNumber;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.UpdateCurrentPatientProfileResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.UpdatePatientByClinicAdminResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.UpdatePatientByPhysiotherapistResource;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;

import java.time.LocalDate;
import java.util.UUID;

public class UpdatePatientCommandFromResourceAssembler {

    public static UpdatePatientByClinicAdminCommand toClinicAdminCommand(String patientId, UpdatePatientByClinicAdminResource resource) {
        return new UpdatePatientByClinicAdminCommand(
                new PatientId(UUID.fromString(patientId)),
                new FirstName(resource.firstName()),
                new LastName(resource.lastName()),
                new Dni(resource.dni()),
                new BirthDate(LocalDate.parse(resource.birthDate())),
                new Gender(resource.gender()),
                new Email(resource.email()),
                new PhoneNumber(resource.countryCode(), resource.phoneNumber()),
                new MedicalCondition(resource.medicalCondition()),
                resource.assignedPhysiotherapistId() == null || resource.assignedPhysiotherapistId().isBlank()
                        ? null
                        : new PhysiotherapistId(UUID.fromString(resource.assignedPhysiotherapistId()))
        );
    }

    public static UpdatePatientByPhysiotherapistCommand toPhysiotherapistCommand(String patientId, UpdatePatientByPhysiotherapistResource resource) {
        return new UpdatePatientByPhysiotherapistCommand(
                new PatientId(UUID.fromString(patientId)),
                new FirstName(resource.firstName()),
                new LastName(resource.lastName()),
                new Email(resource.email()),
                new PhoneNumber(resource.countryCode(), resource.phoneNumber()),
                new MedicalCondition(resource.medicalCondition())
        );
    }

    public static UpdateCurrentPatientProfileCommand toCurrentPatientCommand(String patientId, UpdateCurrentPatientProfileResource resource) {
        return new UpdateCurrentPatientProfileCommand(
                new PatientId(UUID.fromString(patientId)),
                new Email(resource.email()),
                new PhoneNumber(resource.countryCode(), resource.phoneNumber())
        );
    }
}
