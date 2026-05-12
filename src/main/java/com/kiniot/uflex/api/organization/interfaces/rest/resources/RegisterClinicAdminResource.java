package com.kiniot.uflex.api.organization.interfaces.rest.resources;

public record RegisterClinicAdminResource(
        String firstName,
        String lastName,
        String dni,
        String birthDate,
        String gender,
        String countryCode,
        String phoneNumber
) {}