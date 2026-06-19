package com.kiniot.uflex.api.organization.interfaces.rest.resources;

public record PhysiotherapistResource(
        String id,
        String userId,
        String clinicId,
        String fullName,
        String specialty,
        String email,
        String countryCode,
        String phoneNumber,
        String licenseNumber,
        String professionalSummary,
        String photoAssetId,
        String photoUrl,
        int yearsOfExperience,
        String hireDate,
        String status
) {}
