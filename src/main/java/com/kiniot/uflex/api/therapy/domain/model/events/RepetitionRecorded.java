package com.kiniot.uflex.api.therapy.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class RepetitionRecorded extends ApplicationEvent {

    private final String sessionId;
    private final String serieId;
    private final Double peakAngle;
    private final Double achievedRom;
    private final String classification;
    private final LocalDateTime recordedAt;
    private final LocalDateTime occurredAt;

    private RepetitionRecorded(Builder builder) {
        super(builder.source);
        this.sessionId = builder.sessionId;
        this.serieId = builder.serieId;
        this.peakAngle = builder.peakAngle;
        this.achievedRom = builder.achievedRom;
        this.classification = builder.classification;
        this.recordedAt = builder.recordedAt;
        this.occurredAt = LocalDateTime.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Object source;
        private String sessionId;
        private String serieId;
        private Double peakAngle;
        private Double achievedRom;
        private String classification;
        private LocalDateTime recordedAt;

        public Builder source(Object source)         { this.source = source; return this; }
        public Builder sessionId(String v)           { this.sessionId = v; return this; }
        public Builder serieId(String v)             { this.serieId = v; return this; }
        public Builder peakAngle(Double v)           { this.peakAngle = v; return this; }
        public Builder achievedRom(Double v)         { this.achievedRom = v; return this; }
        public Builder classification(String v)      { this.classification = v; return this; }
        public Builder recordedAt(LocalDateTime v)   { this.recordedAt = v; return this; }

        public RepetitionRecorded build()            { return new RepetitionRecorded(this); }
    }
}
