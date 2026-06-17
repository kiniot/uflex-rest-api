package com.kiniot.uflex.api.therapy.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class TherapySessionCompleted extends ApplicationEvent {

    private final String sessionId;
    private final String patientId;
    private final Instant finalizedAt;
    private final Instant occurredAt;

    private TherapySessionCompleted(Builder builder) {
        super(builder.source);
        this.sessionId = builder.sessionId;
        this.patientId = builder.patientId;
        this.finalizedAt = builder.finalizedAt;
        this.occurredAt = Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Object source;
        private String sessionId;
        private String patientId;
        private Instant finalizedAt;

        public Builder source(Object source)     { this.source = source; return this; }
        public Builder sessionId(String v)       { this.sessionId = v; return this; }
        public Builder patientId(String v)       { this.patientId = v; return this; }
        public Builder finalizedAt(Instant v)    { this.finalizedAt = v; return this; }

        public TherapySessionCompleted build()   { return new TherapySessionCompleted(this); }
    }
}
