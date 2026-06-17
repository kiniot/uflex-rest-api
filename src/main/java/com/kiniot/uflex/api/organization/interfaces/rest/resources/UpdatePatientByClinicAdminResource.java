package com.kiniot.uflex.api.organization.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdatePatientByClinicAdminResource(
        @Schema(description = "Patient first name", example = "Lucia")
        String firstName,
        @Schema(description = "Patient last name", example = "Ramos")
        String lastName,
        @Schema(description = "Patient DNI with exactly 8 digits", example = "74839210")
        String dni,
        @Schema(description = "Patient birth date in ISO format", example = "1992-08-14")
        String birthDate,
        @Schema(description = "Patient gender", allowableValues = {"MALE", "FEMALE", "OTHER"}, example = "FEMALE")
        String gender,
        @Schema(description = "Patient email address", example = "lucia.ramos@example.com")
        String email,
        @Schema(description = "Phone country code", example = "+51")
        String countryCode,
        @Schema(description = "Phone number without country code", example = "987654321")
        String phoneNumber,
        @Schema(description = "Medical condition summary", example = "Post-operative knee rehabilitation")
        String medicalCondition,
        @Schema(description = "Assigned physiotherapist ID. Send null or empty to unassign.", example = "019e1e7d-80c3-71c5-ae4b-2358fa9ae43c")
        String assignedPhysiotherapistId
) {}
