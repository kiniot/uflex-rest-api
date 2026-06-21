package com.kiniot.uflex.api.therapy.domain.services;

import com.kiniot.uflex.api.planning.interfaces.acl.dto.DailyRoutineDto;
import com.kiniot.uflex.api.therapy.domain.model.aggregates.TherapySession;
import com.kiniot.uflex.api.therapy.domain.model.entities.Serie;
import com.kiniot.uflex.api.therapy.domain.model.queries.*;

public interface TherapySessionQueryService {

    TherapySession handle(GetTherapySessionByIdQuery query);

    TherapySession handle(GetActiveTherapySessionByPatientIdQuery query);

    TherapySession handle(GetActiveTherapySessionByDeviceSerialQuery query);

    TherapySession handle(GetSessionProgressQuery query);

    TherapySession handle(GetSessionSummaryQuery query);

    Serie handle(GetSerieDetailsQuery query);

    DailyRoutineDto handle(GetDailyScheduleQuery query);
}
