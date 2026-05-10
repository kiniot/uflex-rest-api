package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Address(
        @Column(nullable = false, length = 300)
        String street,
        @Column(nullable = false, length = 100)
        String district,
        @Column(nullable = false, length = 100)
        String province,
        @Column(nullable = false, length = 100)
        String department,
        @Column(nullable = false, length = 50)
        String country,
        @Column(length = 20)
        String postalCode
) {
    public Address {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street cannot be null or blank");
        }
        if (district == null || district.isBlank()) {
            throw new IllegalArgumentException("District cannot be null or blank");
        }
        if (province == null || province.isBlank()) {
            throw new IllegalArgumentException("Province cannot be null or blank");
        }
        if (department == null || department.isBlank()) {
            throw new IllegalArgumentException("Department cannot be null or blank");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country cannot be null or blank");
        }
    }

    public Address() {
        this("", "", "", "", "", null);
    }
}