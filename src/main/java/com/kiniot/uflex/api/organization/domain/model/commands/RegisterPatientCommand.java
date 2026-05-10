package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.*;

public record RegisterPatientCommand(
        FirstName firstName,
        LastName lastName,
        Dni dni,
        BirthDate birthDate,
        Gender gender,
        EmailAddress emailAddress,
        PhoneNumber phoneNumber,
        MedicalCondition medicalCondition
) {
    public RegisterPatientCommand {
        if (firstName == null) {
            throw new IllegalArgumentException("First name cannot be null");
        }
        if (lastName == null) {
            throw new IllegalArgumentException("Last name cannot be null");
        }
        if (dni == null) {
            throw new IllegalArgumentException("DNI cannot be null");
        }
        if (birthDate == null) {
            throw new IllegalArgumentException("Birth date cannot be null");
        }
        if (gender == null) {
            throw new IllegalArgumentException("Gender cannot be null");
        }
        if (emailAddress == null) {
            throw new IllegalArgumentException("Email address cannot be null");
        }
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        }
    }
}