package com.kiniot.uflex.api.organization.interfaces.rest.transform;

import com.kiniot.uflex.api.organization.domain.model.commands.RegisterClinicCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.*;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.AddressResource;
import com.kiniot.uflex.api.organization.interfaces.rest.resources.RegisterClinicResource;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Address;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;

public class RegisterClinicCommandFromResourceAssembler {

    public static RegisterClinicCommand toCommandFromResource(RegisterClinicResource resource, UserId createdBy) {
        return new RegisterClinicCommand(
                new LegalName(resource.legalName()),
                new CommercialName(resource.commercialName()),
                new Ruc(resource.ruc()),
                new Email(resource.email()),
                new PhoneNumber(resource.countryCode(), resource.phoneNumber()),
                toAddress(resource.address()),
                createdBy
        );
    }

    private static Address toAddress(AddressResource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        return new Address(
                resource.countryCode(),
                resource.region(),
                resource.city(),
                resource.addressLine1(),
                resource.addressLine2(),
                resource.postalCode()
        );
    }
}
