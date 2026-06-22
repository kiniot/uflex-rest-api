package com.kiniot.uflex.api.therapy.domain.model.entities;

import com.kiniot.uflex.api.shared.domain.model.entities.AuditableModel;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.CompletedRepetitionId;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.RepetitionClassification;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class CompletedRepetition extends AuditableModel<CompletedRepetitionId> {

    @EmbeddedId
    private CompletedRepetitionId id;

    /** Peak joint angle reached during the repetition (degrees). */
    @Column(nullable = false)
    private Double peakAngle;

    /** Range of motion achieved in the repetition (peak minus the discovered baseline). */
    @Column
    private Double achievedRom;

    /** Quality classification produced by the edge. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepetitionClassification classification;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @Column(columnDefinition = "UUID")
    private UUID edgeSequenceId;

    public CompletedRepetition(Double peakAngle, Double achievedRom, RepetitionClassification classification,
                               LocalDateTime recordedAt, UUID edgeSequenceId) {
        this.id = new CompletedRepetitionId();
        this.peakAngle = peakAngle;
        this.achievedRom = achievedRom;
        this.classification = classification;
        this.recordedAt = recordedAt;
        this.edgeSequenceId = edgeSequenceId;
    }

    @Override
    public CompletedRepetitionId getId() {
        return id;
    }
}
