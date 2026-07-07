package com.kiniot.uflex.api.organization.interfaces.rest.transform;

import com.kiniot.uflex.api.organization.domain.model.commands.UpdatePhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.LicenseNumber;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhoneNumber;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ProfessionalSummary;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.Specialty;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.UpdatePhysiotherapistResource;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;

import java.util.UUID;

public class UpdatePhysiotherapistCommandFromResourceAssembler {

    public static UpdatePhysiotherapistCommand toCommandFromResource(String physiotherapistId, UpdatePhysiotherapistResource resource) {
        return new UpdatePhysiotherapistCommand(
                new PhysiotherapistId(UUID.fromString(physiotherapistId)),
                resource.fullName(),
                Specialty.valueOf(resource.specialty()),
                new Email(resource.email()),
                new PhoneNumber(resource.countryCode(), resource.phoneNumber()),
                new LicenseNumber(resource.licenseNumber()),
                new ProfessionalSummary(resource.professionalSummary()),
                resource.photoAssetId() != null ? UUID.fromString(resource.photoAssetId()) : null,
                resource.yearsOfExperience()
        );
    }
}
