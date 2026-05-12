package com.kiniot.uflex.api.organization.interfaces.rest.resources;

import java.time.LocalDateTime;

public record ClinicResource(
        String id,
        String legalName,
        String commercialName,
        String ruc,
        String email,
        String countryCode,
        String phoneNumber,
        String createdAt,
        String updatedAt
) {}