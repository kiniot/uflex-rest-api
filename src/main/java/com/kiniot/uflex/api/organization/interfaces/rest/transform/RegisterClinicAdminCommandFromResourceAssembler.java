package com.kiniot.uflex.api.organization.interfaces.rest.transform;

import com.kiniot.uflex.api.organization.domain.model.commands.RegisterClinicAdminCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.*;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterClinicAdminResource;

import java.time.LocalDate;

public class RegisterClinicAdminCommandFromResourceAssembler {

    public static RegisterClinicAdminCommand toCommandFromResource(RegisterClinicAdminResource resource) {
        return new RegisterClinicAdminCommand(
                new FirstName(resource.firstName()),
                new LastName(resource.lastName()),
                new Dni(resource.dni()),
                new BirthDate(LocalDate.parse(resource.birthDate())),
                new Gender(resource.gender()),
                new PhoneNumber(resource.countryCode(), resource.phoneNumber())
        );
    }
}