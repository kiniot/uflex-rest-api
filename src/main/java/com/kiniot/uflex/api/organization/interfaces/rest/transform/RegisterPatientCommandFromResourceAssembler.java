package com.kiniot.uflex.api.organization.interfaces.rest.transform;

import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientByClinicAdminCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientByPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.*;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterPatientByClinicAdminResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterPatientByPhysiotherapistResource;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;

import java.time.LocalDate;
import java.util.UUID;

public class RegisterPatientCommandFromResourceAssembler {

    public static RegisterPatientByClinicAdminCommand toRegisterPatientByClinicAdminCommand(RegisterPatientByClinicAdminResource resource) {
        return new RegisterPatientByClinicAdminCommand(
                new FirstName(resource.firstName()),
                new LastName(resource.lastName()),
                new Dni(resource.dni()),
                new BirthDate(LocalDate.parse(resource.birthDate())),
                new Gender(resource.gender()),
                new Email(resource.email()),
                new PhoneNumber(resource.countryCode(), resource.phoneNumber()),
                toMedicalCondition(resource.medicalCondition()),
                resource.assignedPhysiotherapistId() != null && !resource.assignedPhysiotherapistId().isBlank()
                        ? new PhysiotherapistId(UUID.fromString(resource.assignedPhysiotherapistId()))
                        : null
        );
    }

    public static RegisterPatientByPhysiotherapistCommand toRegisterPatientByPhysiotherapistCommand(
            RegisterPatientByPhysiotherapistResource resource,
            PhysiotherapistId assignedPhysiotherapistId
    ) {
        return new RegisterPatientByPhysiotherapistCommand(
                new FirstName(resource.firstName()),
                new LastName(resource.lastName()),
                new Dni(resource.dni()),
                new BirthDate(LocalDate.parse(resource.birthDate())),
                new Gender(resource.gender()),
                new Email(resource.email()),
                new PhoneNumber(resource.countryCode(), resource.phoneNumber()),
                toMedicalCondition(resource.medicalCondition()),
                assignedPhysiotherapistId
        );
    }

    private static MedicalCondition toMedicalCondition(String medicalCondition) {
        return medicalCondition != null
                ? new MedicalCondition(medicalCondition)
                : new MedicalCondition(null);
    }
}
