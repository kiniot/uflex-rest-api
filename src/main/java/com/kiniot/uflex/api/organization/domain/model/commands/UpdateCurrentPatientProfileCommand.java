package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhoneNumber;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;

public record UpdateCurrentPatientProfileCommand(
        PatientId patientId,
        Email emailAddress,
        PhoneNumber phoneNumber
) {}
