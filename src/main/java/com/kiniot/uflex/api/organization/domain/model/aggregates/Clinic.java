package com.kiniot.uflex.api.organization.domain.model.aggregates;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterClinicCommand;
import com.kiniot.uflex.api.organization.domain.model.events.ClinicActivatedEvent;
import com.kiniot.uflex.api.organization.domain.model.events.ClinicArchivedEvent;
import com.kiniot.uflex.api.organization.domain.model.events.ClinicRegisteredEvent;
import com.kiniot.uflex.api.organization.domain.model.events.ClinicSuspendedEvent;
import com.kiniot.uflex.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.*;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;

@Getter
@Entity
public class Clinic extends AuditableAbstractAggregateRoot<Clinic, ClinicId> {

    @EmbeddedId
    private ClinicId id;

    @Embedded
    private LegalName legalName;

    @Embedded
    private CommercialName commercialName;

    @Embedded
    private Ruc ruc;

    @Embedded
    private EmailAddress emailAddress;

    @Embedded
    private PhoneNumber phoneNumber;

    @Embedded
    private ClinicStatus status;

    @Embedded
    private UserId createdBy;

    protected Clinic() {}

    public Clinic(LegalName legalName, CommercialName commercialName, Ruc ruc,
                 EmailAddress emailAddress, PhoneNumber phoneNumber, UserId createdBy) {
        this.id = new ClinicId();
        this.legalName = legalName;
        this.commercialName = commercialName;
        this.ruc = ruc;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.createdBy = createdBy;
        this.status = ClinicStatus.PENDING_ACTIVATION;
    }

    public Clinic(RegisterClinicCommand command) {
        this(command.legalName(), command.commercialName(), command.ruc(),
                command.emailAddress(), command.phoneNumber(), command.createdBy());
    }

    public void register() {
        this.addDomainEvent(new ClinicRegisteredEvent(
                this,
                this.id,
                this.legalName.legalName(),
                this.ruc.ruc(),
                this.createdBy.id().toString()
        ));
    }

    public void activate() {
        if (this.status != ClinicStatus.PENDING_ACTIVATION) {
            throw new IllegalStateException("Clinic can only be activated from PENDING_ACTIVATION status");
        }
        this.status = ClinicStatus.ACTIVE;
        this.addDomainEvent(new ClinicActivatedEvent(this, this.id));
    }

    public void suspend(String reason) {
        if (this.status != ClinicStatus.ACTIVE) {
            throw new IllegalStateException("Only active clinics can be suspended");
        }
        this.status = ClinicStatus.SUSPENDED;
        this.addDomainEvent(new ClinicSuspendedEvent(this, this.id, reason));
    }

    public void archive() {
        if (this.status == ClinicStatus.ARCHIVED) {
            throw new IllegalStateException("Clinic is already archived");
        }
        this.status = ClinicStatus.ARCHIVED;
        this.addDomainEvent(new ClinicArchivedEvent(this, this.id));
    }

    public void updateContactInfo(EmailAddress emailAddress, PhoneNumber phoneNumber) {
        if (this.status == ClinicStatus.ARCHIVED) {
            throw new IllegalStateException("Cannot update archived clinic");
        }
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public ClinicId getId() {
        return id;
    }
}