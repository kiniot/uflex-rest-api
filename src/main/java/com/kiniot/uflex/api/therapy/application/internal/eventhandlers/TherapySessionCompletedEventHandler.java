package com.kiniot.uflex.api.therapy.application.internal.eventhandlers;

import com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl.ExternalPlanningService;
import com.kiniot.uflex.api.therapy.domain.model.events.TherapySessionCompleted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TherapySessionCompletedEventHandler {

    private final ExternalPlanningService externalPlanningService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handle(TherapySessionCompleted event) {
        String finalizedAt = event.getFinalizedAt() != null ? event.getFinalizedAt().toString() : null;
        externalPlanningService.notifyTherapySessionCompleted(
                event.getSessionId(),
                event.getPatientId(),
                finalizedAt
        );
        log.info("TherapySessionCompleted processed: sessionId={}, patientId={}",
                event.getSessionId(), event.getPatientId());
    }
}
