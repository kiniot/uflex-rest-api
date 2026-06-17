package com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.specifications;

import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.TreatmentPlanStatus;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public final class TreatmentPlanSpecifications {

    private TreatmentPlanSpecifications() {
    }

    public static Specification<TreatmentPlan> belongsToClinic(ClinicId clinicId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("clinicId"), clinicId);
    }

    public static Specification<TreatmentPlan> hasPatientId(PatientId patientId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("patientId"), patientId);
    }

    public static Specification<TreatmentPlan> patientIdIn(List<PatientId> patientIds) {
        return (root, query, criteriaBuilder) -> root.get("patientId").in(patientIds);
    }

    public static Specification<TreatmentPlan> hasStatus(TreatmentPlanStatus status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<TreatmentPlan> startsAtOnOrAfter(LocalDate startsAtFrom) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(
                root.get("period").get("startsAt"),
                startsAtFrom
        );
    }

    public static Specification<TreatmentPlan> startsAtOnOrBefore(LocalDate startsAtTo) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(
                root.get("period").get("startsAt"),
                startsAtTo
        );
    }

    public static Specification<TreatmentPlan> endsAtOnOrAfter(LocalDate endsAtFrom) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(
                root.get("period").get("endsAt"),
                endsAtFrom
        );
    }

    public static Specification<TreatmentPlan> endsAtOnOrBefore(LocalDate endsAtTo) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(
                root.get("period").get("endsAt"),
                endsAtTo
        );
    }

    public static Specification<TreatmentPlan> noResults() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
    }
}
