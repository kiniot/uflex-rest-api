package com.kiniot.uflex.api.organization.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdatePhysiotherapistResource(
        @Schema(description = "Physiotherapist full name", example = "Pepito Perez")
        String fullName,

        @Schema(description = "Physiotherapist specialty", allowableValues = {"TRAUMATOLOGICAL", "NEUROLOGICAL", "SPORTS", "GENERAL"}, example = "NEUROLOGICAL")
        String specialty,

        @Schema(description = "Physiotherapist email address", example = "fisio.actualizado@example.com")
        String email,

        @Schema(description = "Phone country code", example = "+51")
        String countryCode,

        @Schema(description = "Phone number without country code", example = "987654321")
        String phoneNumber,

        @Schema(description = "Professional license number", example = "CPT12345")
        String licenseNumber,

        @Schema(description = "Professional summary", example = "Fisioterapeuta especializado en rehabilitacion neurologica con mas de 10 anos de experiencia")
        String professionalSummary,

        @Schema(description = "Media asset id of the profile photo. Send current value to keep it, null to clear it, or a new asset id to replace it.", example = "019e1e7d-80c3-71c5-ae4b-2358fa9ae43c", nullable = true)
        String photoAssetId,

        @Schema(description = "Years of experience", example = "10")
        int yearsOfExperience
) {}
