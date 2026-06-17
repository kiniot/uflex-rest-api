package com.kiniot.uflex.api.therapy.application.internal.eventhandlers;

import com.kiniot.uflex.api.therapy.domain.model.events.SerieStarted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class SerieStartedEventHandler {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handle(SerieStarted event) {
        log.info("SerieStarted processed: sessionId={}, serieId={}", event.getSessionId(), event.getSerieId());
    }
}
