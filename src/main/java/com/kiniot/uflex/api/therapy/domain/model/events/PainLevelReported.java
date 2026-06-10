package com.kiniot.uflex.api.therapy.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class PainLevelReported extends ApplicationEvent {

    private final String sessionId;
    private final String patientId;
    private final Integer painLevel;
    private final Instant occurredAt;

    private PainLevelReported(Builder builder) {
        super(builder.source);
        this.sessionId = builder.sessionId;
        this.patientId = builder.patientId;
        this.painLevel = builder.painLevel;
        this.occurredAt = Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Object source;
        private String sessionId;
        private String patientId;
        private Integer painLevel;

        public Builder source(Object source)     { this.source = source; return this; }
        public Builder sessionId(String v)       { this.sessionId = v; return this; }
        public Builder patientId(String v)       { this.patientId = v; return this; }
        public Builder painLevel(Integer v)      { this.painLevel = v; return this; }

        public PainLevelReported build()         { return new PainLevelReported(this); }
    }
}
