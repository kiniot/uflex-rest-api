package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.interfaces.acl.dto.DailyRoutineDto;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.DailyScheduleResource;

import java.time.LocalDate;
import java.util.UUID;

public final class DailyScheduleResourceFromDtoAssembler {

    private DailyScheduleResourceFromDtoAssembler() {}

    public static DailyScheduleResource toResourceFromDto(UUID patientId, LocalDate date, DailyRoutineDto routine) {
        UUID routineId = routine.routineId() != null ? UUID.fromString(routine.routineId()) : null;
        return new DailyScheduleResource(
                patientId,
                date,
                routine.resolutionStatus(),
                routineId,
                routine.totalSeries(),
                routine.estimatedDurationMinutes());
    }
}
