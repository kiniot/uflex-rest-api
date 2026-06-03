package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.FirstName;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.LastName;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.MedicalCondition;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhoneNumber;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;

public record UpdatePatientByPhysiotherapistCommand(
        PatientId patientId,
        FirstName firstName,
        LastName lastName,
        Email emailAddress,
        PhoneNumber phoneNumber,
        MedicalCondition medicalCondition
) {}
