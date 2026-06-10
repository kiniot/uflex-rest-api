package com.kiniot.uflex.api.therapy.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class HardwareReadinessConfirmed extends ApplicationEvent {

    private final String sessionId;
    private final String deviceId;
    private final Instant occurredAt;

    private HardwareReadinessConfirmed(Builder builder) {
        super(builder.source);
        this.sessionId = builder.sessionId;
        this.deviceId = builder.deviceId;
        this.occurredAt = Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Object source;
        private String sessionId;
        private String deviceId;

        public Builder source(Object source)     { this.source = source; return this; }
        public Builder sessionId(String v)       { this.sessionId = v; return this; }
        public Builder deviceId(String v)        { this.deviceId = v; return this; }

        public HardwareReadinessConfirmed build() { return new HardwareReadinessConfirmed(this); }
    }
}
