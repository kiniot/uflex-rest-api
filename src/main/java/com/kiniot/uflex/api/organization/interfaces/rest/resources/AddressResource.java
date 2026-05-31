package com.kiniot.uflex.api.organization.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record AddressResource(
        @Schema(description = "ISO 3166-1 alpha-2 country code", example = "PE")
        String countryCode,
        String region,
        String city,
        String addressLine1,
        String addressLine2,
        String postalCode
) {}
