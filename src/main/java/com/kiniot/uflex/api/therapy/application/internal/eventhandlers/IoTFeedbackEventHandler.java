package com.kiniot.uflex.api.therapy.application.internal.eventhandlers;

import com.kiniot.uflex.api.therapy.domain.model.events.AnomalousMovementDetected;
import com.kiniot.uflex.api.therapy.domain.model.events.ExcessiveMovementAlertIssued;
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
    public void handle(AnomalousMovementDetected event) {
        log.info("AnomalousMovementDetected processed: sessionId={}, alertType={}, detectedAt={}",
                event.getSessionId(), event.getAlertType(), event.getDetectedAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handle(ExcessiveMovementAlertIssued event) {
        log.info("ExcessiveMovementAlertIssued processed: sessionId={}, alertType={}, detectedAt={}",
                event.getSessionId(), event.getAlertType(), event.getDetectedAt());
    }
}
