package com.kiniot.uflex.api.therapy.application.internal.eventhandlers;

import com.kiniot.uflex.api.therapy.domain.model.events.PainLevelReported;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class PainLevelReportedEventHandler {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handle(PainLevelReported event) {
        log.info("PainLevelReported processed: sessionId={}, patientId={}, painLevel={}",
                event.getSessionId(), event.getPatientId(), event.getPainLevel());
    }
}
