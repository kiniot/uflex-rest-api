package com.kiniot.uflex.api.therapy.application.internal.eventhandlers;

import com.kiniot.uflex.api.therapy.domain.model.events.RepetitionRecorded;
import com.kiniot.uflex.api.therapy.domain.model.events.SerieAchieved;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class RepetitionRecordedEventHandler {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handle(RepetitionRecorded event) {
        log.info("RepetitionRecorded processed: sessionId={}, serieId={}, peakAngle={}, classification={}",
                event.getSessionId(), event.getSerieId(), event.getPeakAngle(), event.getClassification());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handle(SerieAchieved event) {
        log.info("SerieAchieved processed: sessionId={}, serieId={}",
                event.getSessionId(), event.getSerieId());
    }
}
