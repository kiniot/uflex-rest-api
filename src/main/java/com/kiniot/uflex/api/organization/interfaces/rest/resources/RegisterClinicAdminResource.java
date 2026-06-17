package com.kiniot.uflex.api.organization.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterClinicAdminResource(
        String firstName,
        String lastName,
        String dni,
        String birthDate,
        @Schema(allowableValues = {"MALE", "FEMALE", "OTHER"})
        String gender,
        String countryCode,
        String phoneNumber
) {}
