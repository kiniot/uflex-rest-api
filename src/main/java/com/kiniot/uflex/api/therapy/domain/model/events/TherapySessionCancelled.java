package com.kiniot.uflex.api.therapy.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class TherapySessionCancelled extends ApplicationEvent {

    private final String sessionId;
    private final String patientId;
    private final String reason;
    private final Instant cancelledAt;
    private final Instant occurredAt;

    private TherapySessionCancelled(Builder builder) {
        super(builder.source);
        this.sessionId = builder.sessionId;
        this.patientId = builder.patientId;
        this.reason = builder.reason;
        this.cancelledAt = builder.cancelledAt;
        this.occurredAt = Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Object source;
        private String sessionId;
        private String patientId;
        private String reason;
        private Instant cancelledAt;

        public Builder source(Object source)     { this.source = source; return this; }
        public Builder sessionId(String v)       { this.sessionId = v; return this; }
        public Builder patientId(String v)       { this.patientId = v; return this; }
        public Builder reason(String v)          { this.reason = v; return this; }
        public Builder cancelledAt(Instant v)    { this.cancelledAt = v; return this; }

        public TherapySessionCancelled build()   { return new TherapySessionCancelled(this); }
    }
}
