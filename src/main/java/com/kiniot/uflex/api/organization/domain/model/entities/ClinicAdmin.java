package com.kiniot.uflex.api.organization.domain.model.entities;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterClinicAdminCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.*;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;

@Entity
@Getter
public class ClinicAdmin extends AuditableModel<ClinicAdminId> {

    @EmbeddedId
    private ClinicAdminId id;

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

    protected ClinicAdmin() {}

    public ClinicAdmin(ClinicAdminId id, UserId userId, ClinicId clinicId,
                       FirstName firstName, LastName lastName, Dni dni,
                       BirthDate birthDate, Gender gender,
                       EmailAddress emailAddress, PhoneNumber phoneNumber) {
        this.id = id;
        this.userId = userId;
        this.clinicId = clinicId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dni = dni;
        this.birthDate = birthDate;
        this.gender = gender;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
    }

    public ClinicAdmin(RegisterClinicAdminCommand command, UserId userId, ClinicId clinicId, EmailAddress emailAddress) {
        this(new ClinicAdminId(), userId, clinicId,
                command.firstName(), command.lastName(), command.dni(),
                command.birthDate(), command.gender(),
                emailAddress, command.phoneNumber());
    }

    @Override
    public ClinicAdminId getId() {
        return id;
    }
}