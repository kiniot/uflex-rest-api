package com.kiniot.uflex.api.organization.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateCurrentPatientProfileResource(
        @Schema(description = "Patient email address", example = "lucia.updated@example.com")
        String email,
        @Schema(description = "Phone country code", example = "+51")
        String countryCode,
        @Schema(description = "Phone number without country code", example = "998887766")
        String phoneNumber
) {}
