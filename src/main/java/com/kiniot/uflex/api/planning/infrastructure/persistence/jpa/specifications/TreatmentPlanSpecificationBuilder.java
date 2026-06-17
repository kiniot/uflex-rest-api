package com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.specifications;

import com.kiniot.uflex.api.planning.domain.model.aggregates.TreatmentPlan;
import com.kiniot.uflex.api.planning.domain.model.queries.GetAllTreatmentPlansQuery;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class TreatmentPlanSpecificationBuilder {

    private Specification<TreatmentPlan> specification;

    private TreatmentPlanSpecificationBuilder(ClinicId clinicId) {
        this.specification = Specification.where(TreatmentPlanSpecifications.belongsToClinic(clinicId));
    }

    public static TreatmentPlanSpecificationBuilder forClinic(ClinicId clinicId) {
        return new TreatmentPlanSpecificationBuilder(clinicId);
    }

    public TreatmentPlanSpecificationBuilder withQuery(GetAllTreatmentPlansQuery query) {
        if (query.patientId() != null) {
            this.specification = this.specification.and(TreatmentPlanSpecifications.hasPatientId(query.patientId()));
        }
        if (query.status() != null) {
            this.specification = this.specification.and(TreatmentPlanSpecifications.hasStatus(query.status()));
        }
        if (query.startsAtFrom() != null) {
            this.specification = this.specification.and(TreatmentPlanSpecifications.startsAtOnOrAfter(query.startsAtFrom()));
        }
        if (query.startsAtTo() != null) {
            this.specification = this.specification.and(TreatmentPlanSpecifications.startsAtOnOrBefore(query.startsAtTo()));
        }
        if (query.endsAtFrom() != null) {
            this.specification = this.specification.and(TreatmentPlanSpecifications.endsAtOnOrAfter(query.endsAtFrom()));
        }
        if (query.endsAtTo() != null) {
            this.specification = this.specification.and(TreatmentPlanSpecifications.endsAtOnOrBefore(query.endsAtTo()));
        }
        return this;
    }

    public TreatmentPlanSpecificationBuilder withPatientIds(List<PatientId> patientIds) {
        this.specification = this.specification.and(
                patientIds.isEmpty()
                        ? TreatmentPlanSpecifications.noResults()
                        : TreatmentPlanSpecifications.patientIdIn(patientIds)
        );
        return this;
    }

    public Specification<TreatmentPlan> build() {
        return this.specification;
    }
}
