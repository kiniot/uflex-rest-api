package com.kiniot.uflex.api.organization.interfaces.rest.resources;

public record RegisterClinicResource(
        String legalName,
        String commercialName,
        String ruc,
        String email,
        String countryCode,
        String phoneNumber,
        AddressResource address
) {}
