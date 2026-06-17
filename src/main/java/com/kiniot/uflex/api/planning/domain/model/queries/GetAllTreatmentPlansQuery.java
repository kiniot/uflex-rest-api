package com.kiniot.uflex.api.planning.domain.model.queries;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanStatus;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;

import java.time.LocalDate;

public record GetAllTreatmentPlansQuery(
        PatientId patientId,
        PhysiotherapistId physiotherapistId,
        TreatmentPlanStatus status,
        LocalDate startsAtFrom,
        LocalDate startsAtTo,
        LocalDate endsAtFrom,
        LocalDate endsAtTo
) {
}
