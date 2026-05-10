package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ClinicStatus;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.CommercialName;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.EmailAddress;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.LegalName;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhoneNumber;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.Ruc;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;

public record RegisterClinicCommand(
        LegalName legalName,
        CommercialName commercialName,
        Ruc ruc,
        EmailAddress emailAddress,
        PhoneNumber phoneNumber,
        UserId createdBy
) {
    public RegisterClinicCommand {
        if (legalName == null) {
            throw new IllegalArgumentException("Legal name cannot be null");
        }
        if (commercialName == null) {
            throw new IllegalArgumentException("Commercial name cannot be null");
        }
        if (ruc == null) {
            throw new IllegalArgumentException("RUC cannot be null");
        }
        if (emailAddress == null) {
            throw new IllegalArgumentException("Email address cannot be null");
        }
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("Created by cannot be null");
        }
    }
}