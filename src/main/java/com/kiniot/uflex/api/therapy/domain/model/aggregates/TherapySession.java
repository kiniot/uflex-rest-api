package com.kiniot.uflex.api.therapy.domain.model.aggregates;

import com.kiniot.uflex.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.therapy.domain.exceptions.*;
import com.kiniot.uflex.api.therapy.domain.model.commands.InitiateTherapyPreparationCommand;
import com.kiniot.uflex.api.therapy.domain.model.entities.CompensatoryMovement;
import com.kiniot.uflex.api.therapy.domain.model.entities.CompletedRepetition;
import com.kiniot.uflex.api.therapy.domain.model.entities.RoutineExecution;
import com.kiniot.uflex.api.therapy.domain.model.entities.Serie;
import com.kiniot.uflex.api.therapy.domain.model.events.*;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@Entity
public class TherapySession extends AuditableAbstractAggregateRoot<TherapySession, TherapySessionId> {

    @EmbeddedId
    private TherapySessionId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "clinic_id", columnDefinition = "UUID", nullable = false))
    private ClinicId clinicId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "patient_id", columnDefinition = "UUID", nullable = false))
    private PatientId patientId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "treatment_plan_id", columnDefinition = "UUID", nullable = false))
    private TreatmentPlanId treatmentPlanId;

    /**
     * The kit serial of the device measuring this session (see {@link KitSerial}). Links the
     * session to its physical device and its forwarded measurements.
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "iot_device_id", nullable = false))
    private KitSerial iotDeviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "routine_execution_id")
    private RoutineExecution routine;

    @Column(name = "sensors_placed")
    private Boolean sensorsPlaced;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "pain_level"))
    private PainLevel painLevel;

    private Integer painReportsCount;

    private Integer highPainReportsCount;

    private Integer maxReportedPainLevel;

    private Boolean requiresClinicalReview;

    @Column
    private Instant startedAt;

    @Column
    private Instant finalizedAt;

    @Column(length = 500)
    private String cancellationReason;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "therapy_session_id")
    private List<CompensatoryMovement> compensatoryMovements;

    public TherapySession() {
        super();
        this.status = SessionStatus.Pending;
        this.compensatoryMovements = new ArrayList<>();
        this.painReportsCount = 0;
        this.highPainReportsCount = 0;
        this.maxReportedPainLevel = 0;
        this.requiresClinicalReview = false;
    }

    public TherapySession(InitiateTherapyPreparationCommand command, RoutineExecution routine, ClinicId clinicId) {
        this();
        this.id = new TherapySessionId();
        this.clinicId = clinicId;
        this.patientId = PatientId.of(command.patientId());
        this.treatmentPlanId = TreatmentPlanId.of(command.treatmentPlanId());
        this.iotDeviceId = KitSerial.of(command.iotDeviceId());
        this.routine = routine;
    }

    // -------------------------------------------------------------------------
    // Business methods — state transitions (no event publishing)
    // -------------------------------------------------------------------------

    public void confirmHardwareReadiness(boolean sensorsPlaced) {
        if (!sensorsPlaced) {
            throw IoTSensorsNotPlacedException.forSession(TherapySessionId.toStringOrNull(this.id));
        }
        this.sensorsPlaced = true;
        this.status = SessionStatus.Ready;
    }

    public void startSession() {
        if (status != SessionStatus.Ready) {
            throw HardwareNotReadyException.forSession(TherapySessionId.toStringOrNull(id));
        }
        this.status = SessionStatus.InProgress;
        this.startedAt = Instant.now();
        this.routine.start();
    }

    public void finalizeSession() {
        if (!routine.isCompleted()) {
            throw RoutineNotCompletedException.forSession(TherapySessionId.toStringOrNull(id));
        }
        this.status = SessionStatus.Completed;
        this.finalizedAt = Instant.now();
    }

    public void cancelSession(String reason) {
        if (status == SessionStatus.Completed || status == SessionStatus.Cancelled) {
            throw TherapySessionAlreadyFinalizedException.forSession(TherapySessionId.toStringOrNull(id));
        }
        this.status = SessionStatus.Cancelled;
        this.finalizedAt = Instant.now();
        this.cancellationReason = reason;
    }

    public Serie startSerie(SerieId serieId) {
        ensureInProgress();
        Serie serie = routine.findSerie(serieId)
                .orElseThrow(() -> SerieNotFoundException.withId(SerieId.toStringOrNull(serieId)));
        serie.start();
        return serie;
    }

    /**
     * Records a repetition for the given serie. Returns {@code false} when the
     * repetition is a duplicate (same edgeSequenceId) and was ignored, so callers
     * can skip event publication.
     */
    public boolean recordRepetition(SerieId serieId, Double peakAngle, Double achievedRom,
                                    RepetitionClassification classification, LocalDateTime recordedAt, UUID edgeSequenceId) {
        ensureInProgress();
        Serie serie = routine.findSerie(serieId)
                .orElseThrow(() -> SerieNotFoundException.withId(SerieId.toStringOrNull(serieId)));
        if (serie.isDuplicateRepetition(edgeSequenceId)) return false;
        serie.addRepetition(new CompletedRepetition(peakAngle, achievedRom, classification, recordedAt, edgeSequenceId));
        routine.checkCompletion();
        return true;
    }

    public CompensatoryMovement recordCompensatoryMovement(CompensatoryMovementType type) {
        ensureInProgress();
        CompensatoryMovement movement = new CompensatoryMovement(type, Instant.now());
        this.compensatoryMovements.add(movement);
        return movement;
    }

    private void ensureInProgress() {
        if (status != SessionStatus.InProgress) {
            throw TherapySessionNotInProgressException.forSession(TherapySessionId.toStringOrNull(id), status);
        }
    }

    public void reportPainLevel(PainLevel level) {
        ensureInProgress();
        this.painLevel = level;
        this.painReportsCount++;
        this.maxReportedPainLevel = Math.max(this.maxReportedPainLevel, level.value());

        if (level.value() >= 7) {
            this.highPainReportsCount++;
        }

        if (this.highPainReportsCount >= 3 || level.value() == 10) {
            this.requiresClinicalReview = true;
        }
    }

    public Optional<Serie> findSerie(SerieId serieId) {
        return routine.findSerie(serieId);
    }

    public int getCompensatoryMovementsCount() {
        return compensatoryMovements.size();
    }

    // -------------------------------------------------------------------------
    // Event-publishing methods — called by the Application layer after save
    // -------------------------------------------------------------------------

    public TherapyPreparationInitiated publishTherapyPreparationInitiated() {
        return TherapyPreparationInitiated.builder()
                .source(this)
                .sessionId(TherapySessionId.toStringOrNull(this.id))
                .patientId(PatientId.toStringOrNull(this.patientId))
                .treatmentPlanId(TreatmentPlanId.toStringOrNull(this.treatmentPlanId))
                .iotDeviceId(KitSerial.toStringOrNull(this.iotDeviceId))
                .build();
    }

    public HardwareReadinessConfirmed publishHardwareReadinessConfirmed() {
        return HardwareReadinessConfirmed.builder()
                .source(this)
                .sessionId(TherapySessionId.toStringOrNull(this.id))
                .deviceId(KitSerial.toStringOrNull(this.iotDeviceId))
                .build();
    }

    public RoutineStarted publishRoutineStarted() {
        return RoutineStarted.builder()
                .source(this)
                .sessionId(TherapySessionId.toStringOrNull(this.id))
                .routineId(this.routine != null ? RoutineId.toStringOrNull(this.routine.getId()) : null)
                .build();
    }

    public TherapySessionCompleted publishTherapySessionCompleted() {
        return TherapySessionCompleted.builder()
                .source(this)
                .sessionId(TherapySessionId.toStringOrNull(this.id))
                .patientId(PatientId.toStringOrNull(this.patientId))
                .finalizedAt(this.finalizedAt)
                .build();
    }

    public TherapySessionCancelled publishTherapySessionCancelled() {
        return TherapySessionCancelled.builder()
                .source(this)
                .sessionId(TherapySessionId.toStringOrNull(this.id))
                .patientId(PatientId.toStringOrNull(this.patientId))
                .reason(this.cancellationReason)
                .cancelledAt(this.finalizedAt)
                .build();
    }

    public SerieStarted publishSerieStarted(SerieId serieId) {
        return SerieStarted.builder()
                .source(this)
                .sessionId(TherapySessionId.toStringOrNull(this.id))
                .serieId(SerieId.toStringOrNull(serieId))
                .build();
    }

    public RepetitionRecorded publishRepetitionRecorded(SerieId serieId, Double peakAngle, Double achievedRom,
                                                        RepetitionClassification classification, LocalDateTime recordedAt) {
        return RepetitionRecorded.builder()
                .source(this)
                .sessionId(TherapySessionId.toStringOrNull(this.id))
                .serieId(SerieId.toStringOrNull(serieId))
                .peakAngle(peakAngle)
                .achievedRom(achievedRom)
                .classification(RepetitionClassification.toStringOrNull(classification))
                .recordedAt(recordedAt)
                .build();
    }

    public SerieAchieved publishSerieAchieved(SerieId serieId) {
        return SerieAchieved.builder()
                .source(this)
                .sessionId(TherapySessionId.toStringOrNull(this.id))
                .serieId(SerieId.toStringOrNull(serieId))
                .build();
    }

    public CompensatoryMovementDetected publishCompensatoryMovementDetected(CompensatoryMovement movement) {
        return CompensatoryMovementDetected.builder()
                .source(this)
                .sessionId(TherapySessionId.toStringOrNull(this.id))
                .type(CompensatoryMovementType.toStringOrNull(movement.getType()))
                .detectedAt(movement.getDetectedAt())
                .build();
    }

    public PainLevelReported publishPainLevelReported() {
        return PainLevelReported.builder()
                .source(this)
                .sessionId(TherapySessionId.toStringOrNull(this.id))
                .patientId(PatientId.toStringOrNull(this.patientId))
                .painLevel(this.painLevel != null ? this.painLevel.value() : null)
                .build();
    }

    @Override
    public TherapySessionId getId() {
        return id;
    }
}
