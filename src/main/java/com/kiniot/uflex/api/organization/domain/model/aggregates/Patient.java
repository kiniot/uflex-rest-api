package com.kiniot.uflex.api.organization.domain.model.aggregates;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPatientCommand;
import com.kiniot.uflex.api.organization.domain.model.events.PatientAssignedToPhysiotherapistEvent;
import com.kiniot.uflex.api.organization.domain.model.events.PatientProfileRegisteredEvent;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.*;
import com.kiniot.uflex.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class Patient extends AuditableAbstractAggregateRoot<Patient, PatientId> {

    @EmbeddedId
    private PatientId id;

    @Embedded
    private UserId userId;

    @Embedded
    private ClinicId clinicId;

    @Embedded
    private FirstName firstName;

    @Embedded
    private LastName lastName;

    @Embedded
    private Dni dni;

    @Embedded
    private BirthDate birthDate;

    @Embedded
    private Gender gender;

    @Embedded
    private EmailAddress emailAddress;

    @Embedded
    private PhoneNumber phoneNumber;

    @Embedded
    private MedicalCondition medicalCondition;

    @Embedded
    private PhysiotherapistId assignedPhysiotherapistId;

    @Embedded
    private TreatmentPlanId treatmentPlanId;

    @Enumerated(EnumType.STRING)
    private PatientStatus status;

    protected Patient() {}

    public Patient(UserId userId, ClinicId clinicId,
                   FirstName firstName, LastName lastName, Dni dni,
                   BirthDate birthDate, Gender gender,
                   EmailAddress emailAddress, PhoneNumber phoneNumber,
                   MedicalCondition medicalCondition) {
        this.id = new PatientId();
        this.userId = userId;
        this.clinicId = clinicId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dni = dni;
        this.birthDate = birthDate;
        this.gender = gender;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.medicalCondition = medicalCondition;
        this.status = PatientStatus.UNASSIGNED;
    }

    public Patient(RegisterPatientCommand command, UserId userId, ClinicId clinicId, PhysiotherapistId assignedPhysiotherapistId, ClinicId physiotherapistClinicId) {
        this(command, userId, clinicId);
        if (assignedPhysiotherapistId != null && physiotherapistClinicId != null) {
            this.assignPhysiotherapist(assignedPhysiotherapistId, physiotherapistClinicId);
        }
    }

    public Patient(RegisterPatientCommand command, UserId userId, ClinicId clinicId) {
        this(userId, clinicId,
                command.firstName(), command.lastName(), command.dni(),
                command.birthDate(), command.gender(),
                command.emailAddress(), command.phoneNumber(),
                command.medicalCondition());
    }

    public void register() {
        this.addDomainEvent(new PatientProfileRegisteredEvent(
                this,
                this.id,
                this.userId,
                this.clinicId
        ));
    }

    public void assignPhysiotherapist(PhysiotherapistId physiotherapistId, ClinicId physiotherapistClinicId) {
        if (!this.clinicId.equals(physiotherapistClinicId)) {
            throw new IllegalStateException("Cannot assign patient to a physiotherapist from a different clinic");
        }
        if (this.status != PatientStatus.UNASSIGNED) {
            throw new IllegalStateException("Patient can only be assigned from UNASSIGNED status");
        }
        this.assignedPhysiotherapistId = physiotherapistId;
        this.status = PatientStatus.IN_TREATMENT;
        this.addDomainEvent(new PatientAssignedToPhysiotherapistEvent(
                this,
                this.id,
                physiotherapistId,
                this.clinicId
        ));
    }

    public void assignTreatmentPlan(TreatmentPlanId treatmentPlanId) {
        if (this.treatmentPlanId != null) {
            throw new IllegalStateException("Patient already has a treatment plan assigned");
        }
        this.treatmentPlanId = treatmentPlanId;
    }

    public void updateMedicalCondition(MedicalCondition condition) {
        if (this.status != PatientStatus.IN_TREATMENT) {
            throw new IllegalStateException("Cannot update medical condition of patient not in treatment");
        }
        this.medicalCondition = condition;
    }

    public void markInactive() {
        if (this.status != PatientStatus.IN_TREATMENT) {
            throw new IllegalStateException("Only patients in treatment can be marked inactive");
        }
        this.status = PatientStatus.INACTIVE;
    }

    public void reactivate() {
        if (this.status != PatientStatus.INACTIVE) {
            throw new IllegalStateException("Only inactive patients can be reactivated");
        }
        this.status = PatientStatus.IN_TREATMENT;
    }

    public void complete() {
        if (this.status != PatientStatus.IN_TREATMENT) {
            throw new IllegalStateException("Only patients in treatment can be completed");
        }
        this.status = PatientStatus.COMPLETED;
    }

    public void discharge() {
        if (this.status != PatientStatus.COMPLETED && this.status != PatientStatus.INACTIVE) {
            throw new IllegalStateException("Only completed or inactive patients can be discharged");
        }
        this.status = PatientStatus.DISCHARGED;
    }

    @Override
    public PatientId getId() {
        return id;
    }
}