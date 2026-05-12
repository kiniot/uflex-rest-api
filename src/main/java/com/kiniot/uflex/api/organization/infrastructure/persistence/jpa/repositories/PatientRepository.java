package com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Patient;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PatientId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhysiotherapistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, PatientId> {
    Optional<Patient> findByUserId(UserId userId);
    List<Patient> findAllByClinicId(ClinicId clinicId);
    List<Patient> findAllByAssignedPhysiotherapistId(PhysiotherapistId physiotherapistId);
    boolean existsByUserId(UserId userId);
}