package com.kiniot.uflex.api.therapy.domain.services;

import com.kiniot.uflex.api.planning.interfaces.acl.dto.DailyRoutineDto;
import com.kiniot.uflex.api.therapy.domain.model.aggregates.TherapySession;
import com.kiniot.uflex.api.therapy.domain.model.entities.Serie;
import com.kiniot.uflex.api.therapy.domain.model.queries.*;

import java.util.List;

public interface TherapySessionQueryService {

    TherapySession handle(GetTherapySessionByIdQuery query);

    List<TherapySessionHistoryItem> handle(GetTherapySessionHistoryQuery query);

    List<PatientTherapyOverview> handle(GetPatientTherapyOverviewQuery query);

    /**
     * @return the session with {@code routine.series} and their repetitions hydrated, ready to be
     *         walked outside the transaction by the detail assembler
     */
    TherapySession handle(GetTherapySessionDetailQuery query);

    TherapySession handle(GetActiveTherapySessionByPatientIdQuery query);

    TherapySession handle(GetActiveTherapySessionByDeviceSerialQuery query);

    TherapySession handle(GetSessionProgressQuery query);

    TherapySession handle(GetSessionSummaryQuery query);

    Serie handle(GetSerieDetailsQuery query);

    DailyRoutineDto handle(GetDailyScheduleQuery query);

    EdgeConnection handle(GetEdgeConnectionForCurrentPatientQuery query);
}
