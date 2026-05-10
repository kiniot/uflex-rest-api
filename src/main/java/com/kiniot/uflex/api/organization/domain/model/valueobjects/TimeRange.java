package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalTime;

@Embeddable
public record TimeRange(
        @Column(nullable = false)
        LocalTime start,
        @Column(nullable = false)
        LocalTime end
) {
    public TimeRange {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end times cannot be null");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    public TimeRange() {
        this(LocalTime.of(9, 0), LocalTime.of(18, 0));
    }
}