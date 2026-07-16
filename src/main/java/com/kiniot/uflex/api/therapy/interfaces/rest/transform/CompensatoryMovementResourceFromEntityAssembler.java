package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.entities.CompensatoryMovement;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.CompensatoryMovementResource;

public final class CompensatoryMovementResourceFromEntityAssembler {

    private CompensatoryMovementResourceFromEntityAssembler() {}

    public static CompensatoryMovementResource toResourceFromEntity(CompensatoryMovement movement) {
        return CompensatoryMovementResource.builder()
                .movementId(movement.getId() != null ? movement.getId().id() : null)
                .type(movement.getType() != null ? movement.getType().name() : null)
                .detectedAt(movement.getDetectedAt())
                .build();
    }
}
