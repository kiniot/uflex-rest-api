package com.kiniot.uflex.api.organization.interfaces.rest.transform;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Clinic;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.ClinicResource;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ClinicResourceFromEntityAssembler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static String formatDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(FORMATTER);
    }

    public static ClinicResource toResourceFromEntity(Clinic entity) {
        return new ClinicResource(
                entity.getId().id().toString(),
                entity.getLegalName().legalName(),
                entity.getCommercialName().commercialName(),
                entity.getRuc().ruc(),
                entity.getEmailAddress().email(),
                entity.getPhoneNumber().countryCode(),
                entity.getPhoneNumber().number(),
                formatDate(entity.getCreatedAt()),
                formatDate(entity.getUpdatedAt())
        );
    }
}
