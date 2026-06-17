package com.kiniot.uflex.api.planning.domain.model.commands;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.PlanName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanPeriod;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;

import java.util.List;

public record CreateTreatmentPlanCommand(
        PatientId patientId,
        PlanName name,
        TreatmentPlanPeriod period,
        List<CreateTreatmentPlanRoutineCommand> routines
) {
    public CreateTreatmentPlanCommand {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
        if (name == null || name.name().isBlank()) {
            throw new IllegalArgumentException("Plan name cannot be null or blank");
        }
        if (period == null) {
            throw new IllegalArgumentException("Treatment plan period cannot be null");
        }
        if (routines == null || routines.isEmpty()) {
            throw new IllegalArgumentException("Treatment plan must contain at least one routine");
        }
    }
}
