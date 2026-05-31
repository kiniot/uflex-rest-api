package com.kiniot.uflex.api.organization.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterPhysiotherapistResource(
        String fullName,
        @Schema(allowableValues = {"TRAUMATOLOGICAL", "NEUROLOGICAL", "SPORTS", "GENERAL"})
        String specialty,
        String email,
        String countryCode,
        String phoneNumber,
        String licenseNumber,
        String professionalSummary,
        String photoUrl,
        int yearsOfExperience
) {}
