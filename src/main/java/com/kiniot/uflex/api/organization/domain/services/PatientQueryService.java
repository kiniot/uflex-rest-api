package com.kiniot.uflex.api.organization.domain.services;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Patient;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientByIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientByUserIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientsByClinicIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPatientsByPhysiotherapistIdQuery;

import java.util.List;
import java.util.Optional;

public interface PatientQueryService {
    Optional<Patient> handle(GetPatientByIdQuery query);
    Optional<Patient> handle(GetPatientByUserIdQuery query);
    List<Patient> handle(GetPatientsByClinicIdQuery query);
    List<Patient> handle(GetPatientsByPhysiotherapistIdQuery query);
}