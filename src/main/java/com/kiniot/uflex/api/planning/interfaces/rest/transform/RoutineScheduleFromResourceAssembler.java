package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineSchedule;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.RoutineScheduleResource;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class RoutineScheduleFromResourceAssembler {
    public static RoutineSchedule toValueObjectFromResource(RoutineScheduleResource resource) {
        return new RoutineSchedule(
                DayOfWeek.valueOf(resource.dayOfWeek()),
                LocalTime.parse(resource.scheduledTime()));
    }
}
