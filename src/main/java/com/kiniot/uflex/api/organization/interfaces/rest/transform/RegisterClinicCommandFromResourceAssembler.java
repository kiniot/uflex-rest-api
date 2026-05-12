package com.kiniot.uflex.api.organization.interfaces.rest.transform;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterClinicCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.*;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterClinicResource;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;

public class RegisterClinicCommandFromResourceAssembler {

    public static RegisterClinicCommand toCommandFromResource(RegisterClinicResource resource, UserId createdBy) {
        return new RegisterClinicCommand(
                new LegalName(resource.legalName()),
                new CommercialName(resource.commercialName()),
                new Ruc(resource.ruc()),
                new Email(resource.email()),
                new PhoneNumber(resource.countryCode(), resource.phoneNumber()),
                createdBy
        );
    }
}
