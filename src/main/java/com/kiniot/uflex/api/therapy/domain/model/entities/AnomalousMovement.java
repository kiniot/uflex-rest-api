package com.kiniot.uflex.api.therapy.domain.model.entities;

import com.kiniot.uflex.api.shared.domain.model.entities.AuditableModel;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.AlertType;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.AnomalousMovementId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor
public class AnomalousMovement extends AuditableModel<AnomalousMovementId> {

    @EmbeddedId
    private AnomalousMovementId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    @Column(nullable = false)
    private Instant detectedAt;

    public AnomalousMovement(AlertType alertType, Instant detectedAt) {
        this.id = new AnomalousMovementId();
        this.alertType = alertType;
        this.detectedAt = detectedAt;
    }

    @Override
    public AnomalousMovementId getId() {
        return id;
    }
}
