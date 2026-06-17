package com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Physiotherapist;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.LicenseNumber;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhysiotherapistRepository extends JpaRepository<Physiotherapist, PhysiotherapistId> {
    Optional<Physiotherapist> findByUserId(UserId userId);
    List<Physiotherapist> findAllByClinicId(ClinicId clinicId);
    boolean existsByLicenseNumber(LicenseNumber licenseNumber);
    boolean existsByLicenseNumberAndClinicId(LicenseNumber licenseNumber, ClinicId clinicId);
    boolean existsByEmailAddressAndClinicId(Email emailAddress, ClinicId clinicId);
}
