package com.kiniot.uflex.api.organization.interfaces.rest.resources;

public record RegisterPatientByPhysiotherapistResource(
        String firstName,
        String lastName,
        String dni,
        String birthDate,
        String gender,
        String email,
        String countryCode,
        String phoneNumber,
        String medicalCondition
) {}
