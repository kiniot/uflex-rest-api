package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.planning.interfaces.acl.dto.DailyRoutineDto;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.DailyScheduleResource;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public final class DailyScheduleResourceFromDtoAssembler {

    private DailyScheduleResourceFromDtoAssembler() {}

    public static DailyScheduleResource toResourceFromDto(UUID patientId, LocalDate date, Optional<DailyRoutineDto> routine) {
        return routine
                .map(dto -> new DailyScheduleResource(
                        patientId,
                        date,
                        UUID.fromString(dto.routineId()),
                        dto.totalSeries(),
                        dto.estimatedDurationMinutes()))
                .orElseGet(() -> new DailyScheduleResource(patientId, date, null, 0, 0));
    }
}
