package com.kiniot.uflex.api.therapy.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class TherapyPreparationInitiated extends ApplicationEvent {

    private final String sessionId;
    private final String patientId;
    private final String treatmentPlanId;
    private final String iotDeviceId;
    private final Instant occurredAt;

    private TherapyPreparationInitiated(Builder builder) {
        super(builder.source);
        this.sessionId = builder.sessionId;
        this.patientId = builder.patientId;
        this.treatmentPlanId = builder.treatmentPlanId;
        this.iotDeviceId = builder.iotDeviceId;
        this.occurredAt = Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Object source;
        private String sessionId;
        private String patientId;
        private String treatmentPlanId;
        private String iotDeviceId;

        public Builder source(Object source)         { this.source = source; return this; }
        public Builder sessionId(String v)           { this.sessionId = v; return this; }
        public Builder patientId(String v)           { this.patientId = v; return this; }
        public Builder treatmentPlanId(String v)     { this.treatmentPlanId = v; return this; }
        public Builder iotDeviceId(String v)         { this.iotDeviceId = v; return this; }

        public TherapyPreparationInitiated build()   { return new TherapyPreparationInitiated(this); }
    }
}
