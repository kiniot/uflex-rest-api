package com.kiniot.uflex.api.organization.domain.model.aggregates;

import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistAlreadySuspendedException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistNotSuspendedException;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.events.PhysiotherapistProfileActivatedEvent;
import com.kiniot.uflex.api.organization.domain.model.events.PhysiotherapistProfileRegisteredEvent;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.LicenseNumber;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhoneNumber;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhotoUrl;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ProfessionalSummary;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhysiotherapistStatus;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.Specialty;
import com.kiniot.uflex.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
public class Physiotherapist extends AuditableAbstractAggregateRoot<Physiotherapist, PhysiotherapistId> {

    @EmbeddedId
    private PhysiotherapistId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", columnDefinition = "UUID", nullable = false))
    private UserId userId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "clinic_id", columnDefinition = "UUID", nullable = false))
    private ClinicId clinicId;

    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Specialty specialty;

    @Embedded
    private Email emailAddress;

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

    @Enumerated(EnumType.STRING)
    private PhysiotherapistStatus status;

    protected Physiotherapist() {}

    public Physiotherapist(UserId userId, ClinicId clinicId, String fullName, Specialty specialty,
                           Email emailAddress, PhoneNumber phoneNumber, LicenseNumber licenseNumber,
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
        this.status = PhysiotherapistStatus.INACTIVE;
    }

    public Physiotherapist(RegisterPhysiotherapistCommand command, UserId userId, ClinicId clinicId) {
        this(userId, clinicId, command.fullName(), command.specialty(),
                command.emailAddress(), command.phoneNumber(), command.licenseNumber(),
                command.professionalSummary(), command.photoUrl(), command.yearsOfExperience());
    }

    public void register() {
        this.addDomainEvent(new PhysiotherapistProfileRegisteredEvent(
                this,
                this.id,
                this.userId,
                this.clinicId
        ));
    }

    public void activate() {
        if (this.status != PhysiotherapistStatus.INACTIVE) {
            throw new IllegalStateException("Profile can only be activated from INACTIVE status");
        }
        this.status = PhysiotherapistStatus.ACTIVE;
        this.addDomainEvent(new PhysiotherapistProfileActivatedEvent(
                this,
                this.id,
                this.userId,
                this.clinicId
        ));
    }

    public void reactivate(boolean hasAssignedPatientsInCharge) {
        if (this.status != PhysiotherapistStatus.SUSPENDED) {
            throw new PhysiotherapistNotSuspendedException();
        }
        this.status = hasAssignedPatientsInCharge ? PhysiotherapistStatus.ACTIVE : PhysiotherapistStatus.INACTIVE;
    }

    public void synchronizeAvailability(boolean hasAssignedPatientsInCharge) {
        if (this.status == PhysiotherapistStatus.SUSPENDED) {
            return;
        }
        this.status = hasAssignedPatientsInCharge ? PhysiotherapistStatus.ACTIVE : PhysiotherapistStatus.INACTIVE;
    }

    public void suspend() {
        if (this.status == PhysiotherapistStatus.SUSPENDED) {
            throw new PhysiotherapistAlreadySuspendedException();
        }
        this.status = PhysiotherapistStatus.SUSPENDED;
    }

    public void updateProfile(
            String fullName,
            Specialty specialty,
            Email emailAddress,
            PhoneNumber phoneNumber,
            LicenseNumber licenseNumber,
            ProfessionalSummary professionalSummary,
            PhotoUrl photoUrl,
            int yearsOfExperience
    ) {
        this.fullName = fullName;
        this.specialty = specialty;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.licenseNumber = licenseNumber;
        this.professionalSummary = professionalSummary;
        this.photoUrl = photoUrl;
        this.yearsOfExperience = yearsOfExperience;
    }

    @Override
    public PhysiotherapistId getId() {
        return id;
    }
}
