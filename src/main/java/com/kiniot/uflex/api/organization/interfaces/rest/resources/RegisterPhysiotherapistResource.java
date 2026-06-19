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
        @Schema(description = "Optional media asset id of the profile photo", example = "019e1e7d-80c3-71c5-ae4b-2358fa9ae43c", nullable = true)
        String photoAssetId,
        int yearsOfExperience
) {}
