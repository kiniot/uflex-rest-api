package com.kiniot.uflex.api.organization.interfaces.rest.transform;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Patient;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.PatientResource;

import java.time.format.DateTimeFormatter;

public class PatientResourceFromEntityAssembler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static PatientResource toResourceFromEntity(Patient entity) {
        return new PatientResource(
                entity.getId().patientId().toString(),
                entity.getFirstName().firstName(),
                entity.getLastName().lastName(),
                entity.getDni().dni(),
                entity.getBirthDate().birthDate().format(DATE_FORMATTER),
                entity.getGender().gender(),
                entity.getEmailAddress().email(),
                entity.getPhoneNumber().countryCode(),
                entity.getPhoneNumber().number(),
                entity.getMedicalCondition() != null ? entity.getMedicalCondition().condition() : null,
                entity.getAssignedPhysiotherapistId() != null ? entity.getAssignedPhysiotherapistId().physiotherapistId().toString() : null,
                entity.getStatus().name(),
                entity.getClinicId().id().toString()
        );
    }
}
