package com.kiniot.uflex.api.organization.interfaces.rest.transform;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Physiotherapist;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.PhysiotherapistResource;

import java.time.format.DateTimeFormatter;

public class PhysiotherapistResourceFromEntityAssembler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static PhysiotherapistResource toResourceFromEntity(Physiotherapist entity, String photoUrl) {
        return new PhysiotherapistResource(
                entity.getId().physiotherapistId().toString(),
                entity.getUserId().id().toString(),
                entity.getClinicId().id().toString(),
                entity.getFullName(),
                entity.getSpecialty() != null ? entity.getSpecialty().name() : null,
                entity.getEmailAddress().email(),
                entity.getPhoneNumber().countryCode(),
                entity.getPhoneNumber().number(),
                entity.getLicenseNumber().licenseNumber(),
                entity.getProfessionalSummary().summary(),
                entity.getPhotoAssetId() != null ? entity.getPhotoAssetId().toString() : null,
                photoUrl,
                entity.getYearsOfExperience(),
                entity.getHireDate().format(DATE_FORMATTER),
                entity.getStatus().name()
        );
    }
}
