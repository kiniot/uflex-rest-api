package com.kiniot.uflex.api.organization.domain.model.commands;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.BirthDate;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.Dni;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.FirstName;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.Gender;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.LastName;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.MedicalCondition;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhoneNumber;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;

public record UpdatePatientByClinicAdminCommand(
        PatientId patientId,
        FirstName firstName,
        LastName lastName,
        Dni dni,
        BirthDate birthDate,
        Gender gender,
        Email emailAddress,
        PhoneNumber phoneNumber,
        MedicalCondition medicalCondition,
        PhysiotherapistId assignedPhysiotherapistId
) {}
