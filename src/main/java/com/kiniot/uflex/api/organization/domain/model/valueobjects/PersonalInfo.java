package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.time.LocalDate;

@Embeddable
public record PersonalInfo(
        @Column(nullable = false, length = 200)
        String fullName,
        @Column(nullable = false, length = 20)
        String documentNumber,
        @Column
        LocalDate birthDate,
        @Column(length = 20)
        String gender,
        @Embedded
        PhoneNumber phone
) {
    public PersonalInfo {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name cannot be null or blank");
        }
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new IllegalArgumentException("Document number cannot be null or blank");
        }
    }

    public PersonalInfo() {
        this("", "", null, null, null);
    }
}