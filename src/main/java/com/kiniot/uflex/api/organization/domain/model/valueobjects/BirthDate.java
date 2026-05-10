package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.time.Period;

@Embeddable
public record BirthDate(
        @Column(nullable = false)
        LocalDate birthDate
) {
    public BirthDate {
        if (birthDate == null) {
            throw new IllegalArgumentException("Birth date cannot be null");
        }
        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birth date cannot be in the future");
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18) {
            throw new IllegalArgumentException("Patient must be at least 18 years old");
        }
    }

    public BirthDate() {
        this(LocalDate.now());
    }
}