package com.kiniot.uflex.api.organization.interfaces.rest.resources;

public record PatientResource(
        String id,
        String firstName,
        String lastName,
        String dni,
        String birthDate,
        String gender,
        String email,
        String countryCode,
        String phoneNumber,
        String medicalCondition,
        String assignedPhysiotherapistId,
        String treatmentPlanId,
        String status,
        String clinicId
) {}