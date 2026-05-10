package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Embedded;
import jakarta.persistence.Embeddable;

@Embeddable
public record EmergencyContact(
        @Embedded
        PersonalInfo personalInfo,
        @Embedded
        PhoneNumber phone,
        @Column(length = 50)
        String relationship
) {
    public EmergencyContact {
        if (personalInfo == null) {
            throw new IllegalArgumentException("Personal info cannot be null");
        }
        if (phone == null) {
            throw new IllegalArgumentException("Phone cannot be null");
        }
    }

    public EmergencyContact() {
        this(new PersonalInfo(), new PhoneNumber(), null);
    }
}