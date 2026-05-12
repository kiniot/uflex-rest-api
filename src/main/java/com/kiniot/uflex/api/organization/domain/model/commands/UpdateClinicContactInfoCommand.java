package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.EmailAddress;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhoneNumber;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ClinicId;

public record UpdateClinicContactInfoCommand(
        ClinicId clinicId,
        EmailAddress emailAddress,
        PhoneNumber phoneNumber
) {
    public UpdateClinicContactInfoCommand {
        if (clinicId == null) {
            throw new IllegalArgumentException("Clinic ID cannot be null");
        }
        if (emailAddress == null) {
            throw new IllegalArgumentException("Email address cannot be null");
        }
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        }
    }
}