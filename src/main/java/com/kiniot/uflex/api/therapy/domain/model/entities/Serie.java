package com.kiniot.uflex.api.therapy.domain.model.entities;

import com.kiniot.uflex.api.shared.domain.model.entities.AuditableModel;
import com.kiniot.uflex.api.therapy.domain.exceptions.SerieNotStartedException;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.ExerciseId;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SerieId;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SerieStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class Serie extends AuditableModel<SerieId> {

    @EmbeddedId
    private SerieId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "exercise_id", columnDefinition = "UUID", nullable = false))
    private ExerciseId exerciseId;

    @Column(nullable = false)
    private Integer targetRepetitions;

    /**
     * Target range of motion (degrees) the patient should achieve. Prescribed by planning;
     * the edge derives the safe ceiling from it. The backend only stores/exposes this target.
     */
    @Column
    private Double targetRom;

    /** Snapshot of the exercise's movement type (planning vocabulary, kept as String). */
    @Column(length = 40)
    private String movementType;

    /** Snapshot of the exercise's body part (planning vocabulary, kept as String). */
    @Column(length = 40)
    private String bodyPart;

    @Column
    private Integer durationSeconds;

    @Column
    private Integer restDurationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SerieStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "serie_id")
    private List<CompletedRepetition> repetitions = new ArrayList<>();

    public Serie(ExerciseId exerciseId, Integer targetRepetitions, Double targetRom,
                 String movementType, String bodyPart,
                 Integer durationSeconds, Integer restDurationSeconds) {
        this.id = new SerieId();
        this.exerciseId = exerciseId;
        this.targetRepetitions = targetRepetitions;
        this.targetRom = targetRom;
        this.movementType = movementType;
        this.bodyPart = bodyPart;
        this.durationSeconds = durationSeconds;
        this.restDurationSeconds = restDurationSeconds;
        this.status = SerieStatus.Pending;
    }

    public void start() {
        this.status = SerieStatus.Started;
    }

    public void addRepetition(CompletedRepetition repetition) {
        if (this.status != SerieStatus.Started) {
            throw SerieNotStartedException.forSerie(SerieId.toStringOrNull(this.id), this.status);
        }
        this.repetitions.add(repetition);
        if (this.repetitions.size() >= this.targetRepetitions) {
            this.status = SerieStatus.Completed;
        }
    }

    public int getCurrentRepetitions() {
        return repetitions.size();
    }

    public boolean isDuplicateRepetition(UUID edgeSequenceId) {
        if (edgeSequenceId == null) return false;
        return repetitions.stream().anyMatch(r -> edgeSequenceId.equals(r.getEdgeSequenceId()));
    }

    @Override
    public SerieId getId() {
        return id;
    }
}
