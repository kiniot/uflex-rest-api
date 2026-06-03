package com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Patient;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, PatientId> {
    Optional<Patient> findByUserId(UserId userId);
    List<Patient> findAllByClinicId(ClinicId clinicId);
    List<Patient> findAllByAssignedPhysiotherapistId(PhysiotherapistId physiotherapistId);
    List<Patient> findAllByAssignedPhysiotherapistIdAndClinicId(PhysiotherapistId physiotherapistId, ClinicId clinicId);
    boolean existsByAssignedPhysiotherapistId(PhysiotherapistId physiotherapistId);
    boolean existsByIdAndClinicId(PatientId patientId, ClinicId clinicId);
    boolean existsByUserId(UserId userId);
    boolean existsByEmailAddress(Email emailAddress);
}
