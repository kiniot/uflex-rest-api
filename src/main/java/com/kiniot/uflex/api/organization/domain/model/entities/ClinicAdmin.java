package com.kiniot.uflex.api.organization.domain.model.entities;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.AdminScope;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ClinicAdminId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PersonalInfo;
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
    private PersonalInfo personalInfo;

    @Embedded
    private AdminScope scope;

    protected ClinicAdmin() {}

    public ClinicAdmin(ClinicAdminId id, UserId userId, ClinicId clinicId,
                       PersonalInfo personalInfo, AdminScope scope) {
        this.id = id;
        this.userId = userId;
        this.clinicId = clinicId;
        this.personalInfo = personalInfo;
        this.scope = scope;
    }

    @Override
    public ClinicAdminId getId() {
        return id;
    }
}