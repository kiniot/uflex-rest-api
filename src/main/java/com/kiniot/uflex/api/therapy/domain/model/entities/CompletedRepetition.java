package com.kiniot.uflex.api.therapy.domain.model.entities;

import com.kiniot.uflex.api.shared.domain.model.entities.AuditableModel;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.CompletedRepetitionId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class CompletedRepetition extends AuditableModel<CompletedRepetitionId> {

    @EmbeddedId
    private CompletedRepetitionId id;

    @Column(nullable = false)
    private Double achievedAngle;

    @Column(nullable = false)
    private Instant recordedAt;

    @Column(columnDefinition = "UUID")
    private UUID edgeSequenceId;

    public CompletedRepetition(Double achievedAngle, Instant recordedAt, UUID edgeSequenceId) {
        this.id = new CompletedRepetitionId();
        this.achievedAngle = achievedAngle;
        this.recordedAt = recordedAt;
        this.edgeSequenceId = edgeSequenceId;
    }

    @Override
    public CompletedRepetitionId getId() {
        return id;
    }
}
