package com.kiniot.uflex.api.organization.interfaces.rest.transform;

import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.*;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterPhysiotherapistResource;

public class RegisterPhysiotherapistCommandFromResourceAssembler {

    public static RegisterPhysiotherapistCommand toCommandFromResource(RegisterPhysiotherapistResource resource) {
        return new RegisterPhysiotherapistCommand(
                resource.fullName(),
                Specialty.valueOf(resource.specialty().toUpperCase()),
                new EmailAddress(resource.email()),
                new PhoneNumber(resource.countryCode(), resource.phoneNumber()),
                new LicenseNumber(resource.licenseNumber()),
                new ProfessionalSummary(resource.professionalSummary()),
                new PhotoUrl(resource.photoUrl()),
                resource.yearsOfExperience()
        );
    }
}