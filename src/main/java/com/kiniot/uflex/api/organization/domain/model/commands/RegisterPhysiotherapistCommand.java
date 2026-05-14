package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.LicenseNumber;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhoneNumber;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhotoUrl;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ProfessionalSummary;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.Specialty;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;

public record RegisterPhysiotherapistCommand(
        String fullName,
        Specialty specialty,
        Email emailAddress,
        PhoneNumber phoneNumber,
        LicenseNumber licenseNumber,
        ProfessionalSummary professionalSummary,
        PhotoUrl photoUrl,
        int yearsOfExperience
) {
    public RegisterPhysiotherapistCommand {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name cannot be null or blank");
        }
        if (specialty == null) {
            throw new IllegalArgumentException("Specialty cannot be null");
        }
        if (emailAddress == null) {
            throw new IllegalArgumentException("Email address cannot be null");
        }
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        }
        if (licenseNumber == null) {
            throw new IllegalArgumentException("License number cannot be null");
        }
        if (professionalSummary == null) {
            throw new IllegalArgumentException("Professional summary cannot be null");
        }
        if (photoUrl == null) {
            throw new IllegalArgumentException("Photo URL cannot be null");
        }
        if (yearsOfExperience < 0) {
            throw new IllegalArgumentException("Years of experience cannot be negative");
        }
    }
}
