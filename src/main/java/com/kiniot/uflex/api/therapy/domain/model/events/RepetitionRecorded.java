package com.kiniot.uflex.api.therapy.domain.model.events;

import lombok.Getter;
import org.springframework.cglib.core.Local;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
public class RepetitionRecorded extends ApplicationEvent {

    private final String sessionId;
    private final String serieId;
    private final Double achievedAngle;
    private final LocalDateTime recordedAt;
    private final LocalDateTime occurredAt;

    private RepetitionRecorded(Builder builder) {
        super(builder.source);
        this.sessionId = builder.sessionId;
        this.serieId = builder.serieId;
        this.achievedAngle = builder.achievedAngle;
        this.recordedAt = builder.recordedAt;
        this.occurredAt = LocalDateTime.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Object source;
        private String sessionId;
        private String serieId;
        private Double achievedAngle;
        private LocalDateTime recordedAt;

        public Builder source(Object source)         { this.source = source; return this; }
        public Builder sessionId(String v)           { this.sessionId = v; return this; }
        public Builder serieId(String v)             { this.serieId = v; return this; }
        public Builder achievedAngle(Double v)       { this.achievedAngle = v; return this; }
        public Builder recordedAt(LocalDateTime v)         { this.recordedAt = v; return this; }

        public RepetitionRecorded build()            { return new RepetitionRecorded(this); }
    }
}
