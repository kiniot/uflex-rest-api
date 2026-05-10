package com.kiniot.uflex.api.organization.domain.model.aggregates;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.model.events.PhysiotherapistProfileActivatedEvent;
import com.kiniot.uflex.api.organization.domain.model.events.PhysiotherapistProfileRegisteredEvent;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.LicenseNumber;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhoneNumber;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhotoUrl;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhysiotherapistId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ProfessionalSummary;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ProfileStatus;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.Specialty;
import com.kiniot.uflex.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.EmailAddress;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
public class Physiotherapist extends AuditableAbstractAggregateRoot<Physiotherapist, PhysiotherapistId> {

    @EmbeddedId
    private PhysiotherapistId id;

    @Embedded
    private UserId userId;

    @Embedded
    private ClinicId clinicId;

    private String fullName;

    @Embedded
    private Specialty specialty;

    @Embedded
    private EmailAddress emailAddress;

    @Embedded
    private PhoneNumber phoneNumber;

    @Embedded
    private LicenseNumber licenseNumber;

    @Embedded
    private ProfessionalSummary professionalSummary;

    @Embedded
    private PhotoUrl photoUrl;

    private int yearsOfExperience;

    private LocalDate hireDate;

    @Embedded
    private ProfileStatus status;

    protected Physiotherapist() {}

    public Physiotherapist(UserId userId, ClinicId clinicId, String fullName, Specialty specialty,
                           EmailAddress emailAddress, PhoneNumber phoneNumber, LicenseNumber licenseNumber,
                           ProfessionalSummary professionalSummary, PhotoUrl photoUrl, int yearsOfExperience) {
        this.id = new PhysiotherapistId();
        this.userId = userId;
        this.clinicId = clinicId;
        this.fullName = fullName;
        this.specialty = specialty;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.licenseNumber = licenseNumber;
        this.professionalSummary = professionalSummary;
        this.photoUrl = photoUrl;
        this.yearsOfExperience = yearsOfExperience;
        this.hireDate = LocalDate.now();
        this.status = ProfileStatus.PENDING_VALIDATION;
    }

    public void register() {
        this.addDomainEvent(new PhysiotherapistProfileRegisteredEvent(
                this,
                this.id,
                this.userId,
                this.clinicId
        ));
    }

    public void validate() {
        if (this.status != ProfileStatus.PENDING_VALIDATION) {
            throw new IllegalStateException("Profile can only be validated from PENDING_VALIDATION status");
        }
        this.status = ProfileStatus.ACTIVE;
        this.addDomainEvent(new PhysiotherapistProfileActivatedEvent(
                this,
                this.id,
                this.userId,
                this.clinicId
        ));
    }

    public void suspend(String reason) {
        if (this.status != ProfileStatus.ACTIVE) {
            throw new IllegalStateException("Only active profiles can be suspended");
        }
        this.status = ProfileStatus.SUSPENDED;
    }

    public void archive() {
        if (this.status == ProfileStatus.ARCHIVED) {
            throw new IllegalStateException("Profile is already archived");
        }
        this.status = ProfileStatus.ARCHIVED;
    }

    @Override
    public PhysiotherapistId getId() {
        return id;
    }
}