package com.kiniot.uflex.api.therapy.domain.services;

import com.kiniot.uflex.api.therapy.domain.model.aggregates.TherapySession;
import com.kiniot.uflex.api.therapy.domain.model.commands.*;

public interface TherapySessionCommandService {

    TherapySession handle(InitiateTherapyPreparationCommand command);

    TherapySession handle(ConfirmHardwareReadinessCommand command);

    TherapySession handle(StartTherapySessionCommand command);

    TherapySession handle(FinalizeTherapySessionCommand command);

    TherapySession handle(CancelTherapySessionCommand command);

    TherapySession handle(StartSerieCommand command);

    TherapySession handle(RecordValidRepetitionCommand command);

    void handle(RecordCompensatoryMovementCommand command);

    void handle(ReportPainLevelCommand command);
}
