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
        @Schema(description = "HTTP(S) URL of the profile photo", example = "https://example.com/photos/pepe.jpg")
        String photoUrl,
        @Schema(description = "Years of experience", example = "10")
        int yearsOfExperience
) {}
