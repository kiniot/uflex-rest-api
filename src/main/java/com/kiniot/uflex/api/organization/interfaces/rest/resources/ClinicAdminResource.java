package com.kiniot.uflex.api.organization.interfaces.rest.resources;

public record ClinicAdminResource(
        String id,
        String firstName,
        String lastName,
        String dni,
        String birthDate,
        String gender,
        String email,
        String countryCode,
        String phoneNumber,
        String clinicId
) {}