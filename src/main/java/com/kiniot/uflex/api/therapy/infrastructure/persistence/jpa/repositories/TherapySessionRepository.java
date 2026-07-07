package com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.therapy.domain.model.aggregates.TherapySession;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SessionStatus;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.TherapySessionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TherapySessionRepository extends JpaRepository<TherapySession, TherapySessionId> {

    @Query("SELECT ts FROM TherapySession ts WHERE ts.patientId.id = :patientId AND ts.clinicId.id = :clinicId AND ts.status IN :statuses")
    Optional<TherapySession> findActiveByPatientId(
            @Param("patientId") UUID patientId,
            @Param("clinicId") UUID clinicId,
            @Param("statuses") Collection<SessionStatus> statuses
    );

    @Query("SELECT ts FROM TherapySession ts WHERE ts.iotDeviceId.value = :deviceSerial AND ts.clinicId.id = :clinicId AND ts.status IN :statuses")
    Optional<TherapySession> findActiveByIotDeviceId(
            @Param("deviceSerial") String deviceSerial,
            @Param("clinicId") UUID clinicId,
            @Param("statuses") Collection<SessionStatus> statuses
    );

    @Query("SELECT COUNT(ts) > 0 FROM TherapySession ts WHERE ts.patientId.id = :patientId AND ts.clinicId.id = :clinicId AND ts.status IN :statuses")
    boolean existsActiveByPatientId(
            @Param("patientId") UUID patientId,
            @Param("clinicId") UUID clinicId,
            @Param("statuses") Collection<SessionStatus> statuses
    );
}
