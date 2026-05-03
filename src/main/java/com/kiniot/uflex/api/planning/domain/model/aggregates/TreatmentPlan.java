package com.kiniot.uflex.api.planning.domain.model.aggregates;

import com.kiniot.uflex.api.planning.domain.model.commands.CreateTreatmentPlanCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanId;
import com.kiniot.uflex.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;

@Getter
@Entity
public class TreatmentPlan extends AuditableAbstractAggregateRoot<TreatmentPlan, TreatmentPlanId> {

    @EmbeddedId
    private TreatmentPlanId id;

    @Embedded
    private PlanName planName;

    @Embedded
    private ClinicId clinicId;

    protected TreatmentPlan() {}

    public TreatmentPlan(CreateTreatmentPlanCommand command, ClinicId clinicId) {
        this.id = command.id();
        this.planName = command.name();
        this.clinicId = clinicId;
    }
}
