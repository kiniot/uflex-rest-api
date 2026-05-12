package com.kiniot.uflex.api.organization.domain.model.aggregates;

import com.fasterxml.uuid.Generators;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterClinicCommand;
import com.kiniot.uflex.api.organization.domain.model.events.ClinicRegisteredEvent;
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
    private UserId createdBy;

    protected Clinic() {}

    public Clinic(LegalName legalName, CommercialName commercialName, Ruc ruc,
                 EmailAddress emailAddress, PhoneNumber phoneNumber, UserId createdBy) {
        this.id = new ClinicId(Generators.timeBasedEpochGenerator().generate());
        this.legalName = legalName;
        this.commercialName = commercialName;
        this.ruc = ruc;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.createdBy = createdBy;
    }

    public Clinic(RegisterClinicCommand command) {
        this(command.legalName(), command.commercialName(), command.ruc(),
                command.emailAddress(), command.phoneNumber(), command.createdBy());
    }

    public void register() {
        this.addDomainEvent(new ClinicRegisteredEvent(
                this,
                this.createdBy.id().toString(),
                this.id.clinicId().toString()
        ));
    }

    public void updateContactInfo(EmailAddress emailAddress, PhoneNumber phoneNumber) {
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public ClinicId getId() {
        return id;
    }
}
