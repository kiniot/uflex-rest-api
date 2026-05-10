package com.kiniot.uflex.api.organization.domain.model.aggregates;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.model.events.PatientAssignedToPhysiotherapistEvent;
import com.kiniot.uflex.api.organization.domain.model.events.PatientProfileRegisteredEvent;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ClinicalSummary;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.EmergencyContact;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.InsuranceInfo;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PatientId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PersonalInfo;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhysiotherapistId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ProfileStatus;
import com.kiniot.uflex.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
    private PersonalInfo personalInfo;

    @Embedded
    private EmergencyContact emergencyContact;

    @Embedded
    private InsuranceInfo insurance;

    @Embedded
    private ClinicalSummary clinicalSummary;

    @Embedded
    private PhysiotherapistId assignedPhysiotherapistId;

    @Embedded
    private ProfileStatus status;

    protected Patient() {}

    public Patient(UserId userId, ClinicId clinicId, PersonalInfo personalInfo,
                   EmergencyContact emergencyContact, InsuranceInfo insurance) {
        this.id = new PatientId();
        this.userId = userId;
        this.clinicId = clinicId;
        this.personalInfo = personalInfo;
        this.emergencyContact = emergencyContact;
        this.insurance = insurance;
        this.clinicalSummary = new ClinicalSummary();
        this.status = ProfileStatus.ACTIVE;
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
        this.assignedPhysiotherapistId = physiotherapistId;
        this.addDomainEvent(new PatientAssignedToPhysiotherapistEvent(
                this,
                this.id,
                physiotherapistId,
                this.clinicId
        ));
    }

    public void updateClinicalSummary(ClinicalSummary summary) {
        if (this.status != ProfileStatus.ACTIVE) {
            throw new IllegalStateException("Cannot update clinical summary of inactive patient");
        }
        this.clinicalSummary = summary;
    }

    public void updateInsurance(InsuranceInfo insurance) {
        this.insurance = insurance;
    }

    public void discharge(String reason) {
        if (this.status != ProfileStatus.ACTIVE) {
            throw new IllegalStateException("Only active patients can be discharged");
        }
        this.status = ProfileStatus.DISCHARGED;
    }

    public void archive() {
        if (this.status == ProfileStatus.ARCHIVED) {
            throw new IllegalStateException("Patient profile is already archived");
        }
        this.status = ProfileStatus.ARCHIVED;
    }

    @Override
    public PatientId getId() {
        return id;
    }
}