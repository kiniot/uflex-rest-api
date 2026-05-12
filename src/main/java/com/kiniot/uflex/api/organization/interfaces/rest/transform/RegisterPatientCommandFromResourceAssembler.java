package com.kiniot.uflex.api.organization.interfaces.rest.transform;

import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.*;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterPatientResource;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;

import java.time.LocalDate;
import java.util.UUID;

public class RegisterPatientCommandFromResourceAssembler {

    public static RegisterPatientCommand toCommandFromResource(RegisterPatientResource resource, PhysiotherapistId assignedPhysiotherapistId) {
        return new RegisterPatientCommand(
                new FirstName(resource.firstName()),
                new LastName(resource.lastName()),
                new Dni(resource.dni()),
                new BirthDate(LocalDate.parse(resource.birthDate())),
                new Gender(resource.gender()),
                new Email(resource.email()),
                new PhoneNumber(resource.countryCode(), resource.phoneNumber()),
                resource.medicalCondition() != null ? new MedicalCondition(resource.medicalCondition()) : new MedicalCondition(null),
                assignedPhysiotherapistId
        );
    }

    public static RegisterPatientCommand toCommandFromResource(RegisterPatientResource resource) {
        return new RegisterPatientCommand(
                new FirstName(resource.firstName()),
                new LastName(resource.lastName()),
                new Dni(resource.dni()),
                new BirthDate(LocalDate.parse(resource.birthDate())),
                new Gender(resource.gender()),
                new Email(resource.email()),
                new PhoneNumber(resource.countryCode(), resource.phoneNumber()),
                resource.medicalCondition() != null ? new MedicalCondition(resource.medicalCondition()) : new MedicalCondition(null),
                null
        );
    }
}
