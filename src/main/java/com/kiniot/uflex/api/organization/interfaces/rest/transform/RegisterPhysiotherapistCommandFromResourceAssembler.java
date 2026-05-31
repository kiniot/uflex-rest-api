package com.kiniot.uflex.api.organization.interfaces.rest.transform;

import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.*;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterPhysiotherapistResource;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;

public class RegisterPhysiotherapistCommandFromResourceAssembler {

    public static RegisterPhysiotherapistCommand toCommandFromResource(RegisterPhysiotherapistResource resource) {
        return new RegisterPhysiotherapistCommand(
                resource.fullName(),
                parseSpecialty(resource.specialty()),
                new Email(resource.email()),
                new PhoneNumber(resource.countryCode(), resource.phoneNumber()),
                new LicenseNumber(resource.licenseNumber()),
                new ProfessionalSummary(resource.professionalSummary()),
                new PhotoUrl(resource.photoUrl()),
                resource.yearsOfExperience()
        );
    }

    private static Specialty parseSpecialty(String specialty) {
        if (specialty == null || specialty.isBlank()) {
            throw new IllegalArgumentException("Specialty cannot be null or blank");
        }

        try {
            return Specialty.valueOf(specialty.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid specialty '%s'. Allowed values: %s"
                            .formatted(specialty, String.join(", ", Specialty.valuesAsStrings()))
            );
        }
    }
}
