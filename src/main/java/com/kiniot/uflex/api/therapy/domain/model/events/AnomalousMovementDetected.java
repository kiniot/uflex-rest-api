package com.kiniot.uflex.api.therapy.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class AnomalousMovementDetected extends ApplicationEvent {

    private final String sessionId;
    private final String alertType;
    private final Instant detectedAt;
    private final Instant occurredAt;

    private AnomalousMovementDetected(Builder builder) {
        super(builder.source);
        this.sessionId = builder.sessionId;
        this.alertType = builder.alertType;
        this.detectedAt = builder.detectedAt;
        this.occurredAt = Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Object source;
        private String sessionId;
        private String alertType;
        private Instant detectedAt;

        public Builder source(Object source)     { this.source = source; return this; }
        public Builder sessionId(String v)       { this.sessionId = v; return this; }
        public Builder alertType(String v)       { this.alertType = v; return this; }
        public Builder detectedAt(Instant v)     { this.detectedAt = v; return this; }

        public AnomalousMovementDetected build() { return new AnomalousMovementDetected(this); }
    }
}
