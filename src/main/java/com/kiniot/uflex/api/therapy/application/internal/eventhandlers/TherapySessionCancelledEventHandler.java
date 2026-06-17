package com.kiniot.uflex.api.therapy.application.internal.eventhandlers;

import com.kiniot.uflex.api.therapy.domain.model.events.TherapySessionCancelled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class TherapySessionCancelledEventHandler {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handle(TherapySessionCancelled event) {
        log.info("TherapySessionCancelled processed: sessionId={}, patientId={}, reason={}",
                event.getSessionId(), event.getPatientId(), event.getReason());
    }
}
