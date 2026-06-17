package com.kiniot.uflex.api.therapy.domain.model.queries;

import java.time.LocalDate;
import java.util.UUID;

public record GetDailyScheduleQuery(UUID patientId, LocalDate date) {}
