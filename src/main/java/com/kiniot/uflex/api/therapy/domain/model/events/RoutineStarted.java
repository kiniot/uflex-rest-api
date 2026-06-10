package com.kiniot.uflex.api.therapy.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class RoutineStarted extends ApplicationEvent {

    private final String sessionId;
    private final String routineId;
    private final Instant occurredAt;

    private RoutineStarted(Builder builder) {
        super(builder.source);
        this.sessionId = builder.sessionId;
        this.routineId = builder.routineId;
        this.occurredAt = Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Object source;
        private String sessionId;
        private String routineId;

        public Builder source(Object source)     { this.source = source; return this; }
        public Builder sessionId(String v)       { this.sessionId = v; return this; }
        public Builder routineId(String v)       { this.routineId = v; return this; }

        public RoutineStarted build()            { return new RoutineStarted(this); }
    }
}
