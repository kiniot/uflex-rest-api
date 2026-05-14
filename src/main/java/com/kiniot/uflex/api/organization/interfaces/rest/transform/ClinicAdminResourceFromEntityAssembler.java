package com.kiniot.uflex.api.organization.interfaces.rest.transform;

import com.kiniot.uflex.api.organization.domain.model.entities.ClinicAdmin;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.ClinicAdminResource;

import java.time.format.DateTimeFormatter;

public class ClinicAdminResourceFromEntityAssembler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static ClinicAdminResource toResourceFromEntity(ClinicAdmin entity) {
        return new ClinicAdminResource(
                entity.getId().clinicAdminId().toString(),
                entity.getFirstName().firstName(),
                entity.getLastName().lastName(),
                entity.getDni().dni(),
                entity.getBirthDate().birthDate().format(FORMATTER),
                entity.getGender().gender(),
                entity.getEmailAddress().email(),
                entity.getPhoneNumber().countryCode(),
                entity.getPhoneNumber().number(),
                entity.getClinicId().id().toString()
        );
    }
}
