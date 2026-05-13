package com.kiniot.uflex.api.organization.interfaces.rest.transform;

import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientByClinicAdminCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientByPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.*;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterPatientResource;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;

import java.time.LocalDate;

public class RegisterPatientCommandFromResourceAssembler {

    public static RegisterPatientByClinicAdminCommand toRegisterPatientByClinicAdminCommand(RegisterPatientResource resource) {
        return new RegisterPatientByClinicAdminCommand(
                new FirstName(resource.firstName()),
                new LastName(resource.lastName()),
                new Dni(resource.dni()),
                new BirthDate(LocalDate.parse(resource.birthDate())),
                new Gender(resource.gender()),
                new Email(resource.email()),
                new PhoneNumber(resource.countryCode(), resource.phoneNumber()),
                toMedicalCondition(resource)
        );
    }

    public static RegisterPatientByPhysiotherapistCommand toRegisterPatientByPhysiotherapistCommand(
            RegisterPatientResource resource,
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
                toMedicalCondition(resource),
                assignedPhysiotherapistId
        );
    }

    private static MedicalCondition toMedicalCondition(RegisterPatientResource resource) {
        return resource.medicalCondition() != null
                ? new MedicalCondition(resource.medicalCondition())
                : new MedicalCondition(null);
    }
}
