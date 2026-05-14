package com.kiniot.uflex.api.planning.domain.model.valueobjects;

public record PlanName(
        String name
) {
    public PlanName {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Plan name cannot be null or blank");
        }
    }
}
