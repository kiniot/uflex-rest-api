package com.kiniot.uflex.api.therapy.domain.model.entities;

import com.kiniot.uflex.api.shared.domain.model.entities.AuditableModel;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.CompensatoryMovementId;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.CompensatoryMovementType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor
public class CompensatoryMovement extends AuditableModel<CompensatoryMovementId> {

    @EmbeddedId
    private CompensatoryMovementId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompensatoryMovementType type;

    @Column(nullable = false)
    private Instant detectedAt;

    public CompensatoryMovement(CompensatoryMovementType type, Instant detectedAt) {
        this.id = new CompensatoryMovementId();
        this.type = type;
        this.detectedAt = detectedAt;
    }

    @Override
    public CompensatoryMovementId getId() {
        return id;
    }
}
