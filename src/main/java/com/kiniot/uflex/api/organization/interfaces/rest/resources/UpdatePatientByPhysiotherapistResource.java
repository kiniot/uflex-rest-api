package com.kiniot.uflex.api.organization.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdatePatientByPhysiotherapistResource(
        @Schema(description = "Patient first name", example = "Mateo")
        String firstName,
        @Schema(description = "Patient last name", example = "Salazar")
        String lastName,
        @Schema(description = "Patient email address", example = "mateo.salazar@example.com")
        String email,
        @Schema(description = "Phone country code", example = "+51")
        String countryCode,
        @Schema(description = "Phone number without country code", example = "912345678")
        String phoneNumber,
        @Schema(description = "Medical condition summary", example = "Shoulder mobility recovery")
        String medicalCondition
) {}
