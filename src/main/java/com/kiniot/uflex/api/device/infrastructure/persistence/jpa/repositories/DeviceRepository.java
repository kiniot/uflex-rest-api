package com.kiniot.uflex.api.device.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.device.domain.model.aggregates.Device;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceId;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceStatus;
import com.kiniot.uflex.api.device.domain.model.valueobjects.MacAddress;
import com.kiniot.uflex.api.device.domain.model.valueobjects.SerialNumber;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, DeviceId> {

    Optional<Device> findBySerialNumber(SerialNumber serialNumber);

    boolean existsBySerialNumber(SerialNumber serialNumber);

    boolean existsByMacAddress(MacAddress macAddress);

    List<Device> findAllByClinicId(ClinicId clinicId);

    List<Device> findAllByClinicIdAndStatus(ClinicId clinicId, DeviceStatus status);

    Optional<Device> findByCurrentPatientId(PatientId patientId);

    @Query("SELECT d FROM Device d WHERE d.clinicId = :clinicId AND d.status = :status")
    List<Device> findByClinicIdAndStatus(@Param("clinicId") ClinicId clinicId, @Param("status") DeviceStatus status);

    @Query("SELECT d FROM Device d WHERE d.clinicId = :clinicId")
    List<Device> findByClinicId(@Param("clinicId") ClinicId clinicId);

    long countByClinicId(ClinicId clinicId);

    long countByClinicIdAndStatus(ClinicId clinicId, DeviceStatus status);

    @Query("SELECT d FROM Device d WHERE d.clinicId.id IS NULL")
    List<Device> findAllInStock();

    @Query("SELECT d FROM Device d WHERE d.clinicId.id IS NULL AND d.status = :status")
    List<Device> findAllInStockByStatus(@Param("status") DeviceStatus status);

    @Query("SELECT COUNT(d) FROM Device d WHERE d.clinicId.id IS NULL")
    long countInStock();

    @Query("SELECT COUNT(d) FROM Device d WHERE d.clinicId = :clinicId AND d.status <> :status")
    long countByClinicIdAndStatusNot(@Param("clinicId") ClinicId clinicId, @Param("status") DeviceStatus status);

    long countByStatus(DeviceStatus status);

    /** Device counts grouped by owning clinic (excludes stock). Each row is [clinicId (UUID), count (Long)]. */
    @Query("SELECT d.clinicId.id, COUNT(d) FROM Device d WHERE d.clinicId.id IS NOT NULL GROUP BY d.clinicId.id")
    List<Object[]> countGroupedByClinicId();
}
