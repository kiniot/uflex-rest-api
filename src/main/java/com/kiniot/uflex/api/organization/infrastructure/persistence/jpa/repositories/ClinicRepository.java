package com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Clinic;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.Ruc;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, ClinicId> {
    Optional<Clinic> findByRuc(Ruc ruc);
    boolean existsByRuc(Ruc ruc);
}
