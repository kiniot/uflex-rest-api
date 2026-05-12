package com.kiniot.uflex.api.organization.interfaces.rest.resources;

public record RegisterPhysiotherapistResource(
        String fullName,
        String specialty,
        String email,
        String countryCode,
        String phoneNumber,
        String licenseNumber,
        String professionalSummary,
        String photoUrl,
        int yearsOfExperience
) {}