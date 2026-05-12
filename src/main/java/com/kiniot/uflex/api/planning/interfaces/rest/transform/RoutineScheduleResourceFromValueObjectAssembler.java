package com.kiniot.uflex.api.planning.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineSchedule;
import com.kiniot.uflex.api.planning.interfaces.rest.resources.RoutineScheduleResource;

public class RoutineScheduleResourceFromValueObjectAssembler {
    public static RoutineScheduleResource toResourceFromValueObject(RoutineSchedule valueObject) {
        return new RoutineScheduleResource(valueObject.dayOfWeek().name(), valueObject.scheduledTime().toString());
    }
}
