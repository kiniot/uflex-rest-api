package com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.model.entities.ClinicAdmin;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ClinicAdminId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ClinicId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicAdminRepository extends JpaRepository<ClinicAdmin, ClinicAdminId> {
    Optional<ClinicAdmin> findByUserId(UserId userId);
    Optional<ClinicAdmin> findByClinicId(ClinicId clinicId);
    boolean existsByUserId(UserId userId);
}