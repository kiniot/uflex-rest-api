package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.LicenseNumber;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhoneNumber;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ProfessionalSummary;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.Specialty;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;

import java.util.UUID;

public record UpdatePhysiotherapistCommand(
        PhysiotherapistId physiotherapistId,
        String fullName,
        Specialty specialty,
        Email emailAddress,
        PhoneNumber phoneNumber,
        LicenseNumber licenseNumber,
        ProfessionalSummary professionalSummary,
        UUID photoAssetId,
        int yearsOfExperience
) {}
