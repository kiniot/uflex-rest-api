package com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.therapy.domain.model.aggregates.TherapySession;
import com.kiniot.uflex.api.therapy.domain.model.entities.Serie;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.RepetitionClassification;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SerieStatus;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SessionStatus;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.TherapySessionId;
import com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections.CompensatoryMovementCountRow;
import com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections.PatientTherapyOverviewRow;
import com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections.SerieRepetitionAggregateRow;
import com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections.TherapySessionHistoryRow;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
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

    // -------------------------------------------------------------------------
    // History read path (clinic web). Three constant-cost queries stitched by the
    // query service; see TherapySessionHistoryRow for why the graph is not fetched.
    // -------------------------------------------------------------------------

    /**
     * A patient's sessions, newest first. Ordering falls back to {@code createdAt} so that sessions
     * cancelled before ever starting (null {@code startedAt}) keep a stable place instead of
     * dropping to an arbitrary position.
     */
    @Query("""
            SELECT new com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections.TherapySessionHistoryRow(
                ts.id.id, ts.status, ts.startedAt, ts.finalizedAt,
                ts.treatmentPlanId.id, r.planningRoutineId,
                ts.painLevel.value, ts.maxReportedPainLevel, ts.requiresClinicalReview, ts.createdAt)
            FROM TherapySession ts
            LEFT JOIN ts.routine r
            WHERE ts.patientId.id = :patientId
              AND ts.clinicId.id = :clinicId
            ORDER BY ts.startedAt DESC NULLS LAST, ts.createdAt DESC
            """)
    List<TherapySessionHistoryRow> findHistoryByPatientId(
            @Param("patientId") UUID patientId,
            @Param("clinicId") UUID clinicId,
            Limit limit
    );

    /**
     * Same as {@link #findHistoryByPatientId}, scoped to one treatment plan.
     *
     * <p>A separate method rather than a {@code (:treatmentPlanId IS NULL OR ...)} guard on the one
     * above: Postgres cannot infer a bind's type when the parameter only ever appears in
     * {@code ? IS NULL}, and fails to prepare the statement with "could not determine data type of
     * parameter". Duplicating the projection is the cheap, obvious way out.
     */
    @Query("""
            SELECT new com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections.TherapySessionHistoryRow(
                ts.id.id, ts.status, ts.startedAt, ts.finalizedAt,
                ts.treatmentPlanId.id, r.planningRoutineId,
                ts.painLevel.value, ts.maxReportedPainLevel, ts.requiresClinicalReview, ts.createdAt)
            FROM TherapySession ts
            LEFT JOIN ts.routine r
            WHERE ts.patientId.id = :patientId
              AND ts.clinicId.id = :clinicId
              AND ts.treatmentPlanId.id = :treatmentPlanId
            ORDER BY ts.startedAt DESC NULLS LAST, ts.createdAt DESC
            """)
    List<TherapySessionHistoryRow> findHistoryByPatientIdAndTreatmentPlanId(
            @Param("patientId") UUID patientId,
            @Param("clinicId") UUID clinicId,
            @Param("treatmentPlanId") UUID treatmentPlanId,
            Limit limit
    );

    /**
     * Per-session series/repetition aggregates. {@code COUNT(CASE ...)} rather than
     * {@code SUM(CASE ...)}: COUNT yields 0 for a session with no repetitions, SUM yields null.
     */
    @Query("""
            SELECT new com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections.SerieRepetitionAggregateRow(
                ts.id.id,
                COUNT(DISTINCT ser.id.id),
                COUNT(DISTINCT CASE WHEN ser.status = :completedSerieStatus THEN ser.id.id END),
                COUNT(rep.id.id),
                COUNT(CASE WHEN rep.classification = :good THEN 1 END),
                COUNT(CASE WHEN rep.classification = :incomplete THEN 1 END),
                COUNT(CASE WHEN rep.classification = :unsafe THEN 1 END),
                AVG(rep.achievedRom))
            FROM TherapySession ts
            LEFT JOIN ts.routine r
            LEFT JOIN r.series ser
            LEFT JOIN ser.repetitions rep
            WHERE ts.id.id IN :sessionIds
            GROUP BY ts.id.id
            """)
    List<SerieRepetitionAggregateRow> aggregateSeriesAndRepetitions(
            @Param("sessionIds") Collection<UUID> sessionIds,
            @Param("completedSerieStatus") SerieStatus completedSerieStatus,
            @Param("good") RepetitionClassification good,
            @Param("incomplete") RepetitionClassification incomplete,
            @Param("unsafe") RepetitionClassification unsafe
    );

    /** Per-session compensatory-movement count, isolated from the repetition join on purpose. */
    @Query("""
            SELECT new com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections.CompensatoryMovementCountRow(
                ts.id.id, COUNT(cm.id.id))
            FROM TherapySession ts
            LEFT JOIN ts.compensatoryMovements cm
            WHERE ts.id.id IN :sessionIds
            GROUP BY ts.id.id
            """)
    List<CompensatoryMovementCountRow> countCompensatoryMovements(@Param("sessionIds") Collection<UUID> sessionIds);

    /**
     * One row per patient for the clinician's index: how much therapy they have done and how well.
     *
     * <p>Sessions with no repetitions still count towards {@code totalSessions} — a session that
     * recorded nothing is itself the finding — but contribute no ROM, hence the AVG skipping nulls.
     */
    @Query("""
            SELECT new com.kiniot.uflex.api.therapy.infrastructure.persistence.jpa.repositories.projections.PatientTherapyOverviewRow(
                ts.patientId.id,
                COUNT(DISTINCT ts.id.id),
                COUNT(DISTINCT CASE WHEN ts.status = :completedStatus THEN ts.id.id END),
                COUNT(DISTINCT CASE WHEN ts.requiresClinicalReview = true THEN ts.id.id END),
                MAX(ts.startedAt),
                COUNT(rep.id.id),
                COUNT(CASE WHEN rep.classification = :good THEN 1 END),
                AVG(rep.achievedRom))
            FROM TherapySession ts
            LEFT JOIN ts.routine r
            LEFT JOIN r.series ser
            LEFT JOIN ser.repetitions rep
            WHERE ts.patientId.id IN :patientIds
              AND ts.clinicId.id = :clinicId
            GROUP BY ts.patientId.id
            """)
    List<PatientTherapyOverviewRow> aggregateTherapyOverviewByPatient(
            @Param("patientIds") Collection<UUID> patientIds,
            @Param("clinicId") UUID clinicId,
            @Param("completedStatus") SessionStatus completedStatus,
            @Param("good") RepetitionClassification good
    );

    /** Which of these patients are mid-session right now, so the index can mark them as live. */
    @Query("""
            SELECT DISTINCT ts.patientId.id FROM TherapySession ts
            WHERE ts.patientId.id IN :patientIds
              AND ts.clinicId.id = :clinicId
              AND ts.status IN :statuses
            """)
    List<UUID> findPatientIdsWithActiveSession(
            @Param("patientIds") Collection<UUID> patientIds,
            @Param("clinicId") UUID clinicId,
            @Param("statuses") Collection<SessionStatus> statuses
    );

    /**
     * Hydrates the persistence context with the session's series and their repetitions in one shot,
     * so the detail assembler can walk the graph without N+1 (open-in-view is off).
     *
     * <p>Only {@code ser.repetitions} is fetch-joined: it is the single bag in this plan, since the
     * session's own EAGER {@code compensatoryMovements} bag is loaded by the {@code findById} that
     * precedes this call. Adding a second collection fetch here would raise
     * {@code MultipleBagFetchException}. Same idiom as
     * {@code TreatmentPlanQueryServiceImpl.loadExerciseSeriesForRoutines}.
     */
    @Query("""
            SELECT ser FROM TherapySession ts
            JOIN ts.routine r
            JOIN r.series ser
            LEFT JOIN FETCH ser.repetitions
            WHERE ts.id.id = :sessionId
            """)
    List<Serie> findSeriesWithRepetitionsBySessionId(@Param("sessionId") UUID sessionId);
}
