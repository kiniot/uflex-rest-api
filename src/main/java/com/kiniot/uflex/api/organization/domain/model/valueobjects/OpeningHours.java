package com.kiniot.uflex.api.organization.domain.model.valueobjects;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;

import java.time.DayOfWeek;
import java.util.Map;

@Embeddable
public record OpeningHours(
        @ElementCollection
        @MapKeyColumn(name = "day_of_week")
        @CollectionTable(name = "opening_hours", joinColumns = @JoinColumn(name = "clinic_id"))
        @Column(name = "time_range")
        Map<DayOfWeek, TimeRange> schedule
) {
    public OpeningHours {
        if (schedule == null || schedule.isEmpty()) {
            throw new IllegalArgumentException("Opening hours schedule cannot be null or empty");
        }
    }

    public OpeningHours() {
        this(Map.of(
                DayOfWeek.MONDAY, new TimeRange(),
                DayOfWeek.TUESDAY, new TimeRange(),
                DayOfWeek.WEDNESDAY, new TimeRange(),
                DayOfWeek.THURSDAY, new TimeRange(),
                DayOfWeek.FRIDAY, new TimeRange()
        ));
    }
}