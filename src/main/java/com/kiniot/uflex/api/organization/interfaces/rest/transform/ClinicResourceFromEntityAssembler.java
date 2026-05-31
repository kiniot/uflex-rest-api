package com.kiniot.uflex.api.organization.interfaces.rest.transform;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Clinic;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.ClinicResource;

public class ClinicResourceFromEntityAssembler {

    public static ClinicResource toResourceFromEntity(Clinic entity) {
        return new ClinicResource(
                entity.getId().id().toString(),
                entity.getLegalName().legalName(),
                entity.getCommercialName().commercialName(),
                entity.getRuc().ruc(),
                entity.getEmailAddress().email(),
                entity.getPhoneNumber().countryCode(),
                entity.getPhoneNumber().number()
        );
    }
}
