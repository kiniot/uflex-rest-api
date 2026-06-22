package com.kiniot.uflex.api.therapy.application.internal.eventhandlers;

import com.kiniot.uflex.api.therapy.domain.model.events.CompensatoryMovementDetected;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class IoTFeedbackEventHandler {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handle(CompensatoryMovementDetected event) {
        log.info("CompensatoryMovementDetected processed: sessionId={}, type={}, detectedAt={}",
                event.getSessionId(), event.getType(), event.getDetectedAt());
    }
}
