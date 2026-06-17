package com.kiniot.uflex.api.therapy.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class SerieStarted extends ApplicationEvent {

    private final String sessionId;
    private final String serieId;
    private final Instant occurredAt;

    private SerieStarted(Builder builder) {
        super(builder.source);
        this.sessionId = builder.sessionId;
        this.serieId = builder.serieId;
        this.occurredAt = Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Object source;
        private String sessionId;
        private String serieId;

        public Builder source(Object source)     { this.source = source; return this; }
        public Builder sessionId(String v)       { this.sessionId = v; return this; }
        public Builder serieId(String v)         { this.serieId = v; return this; }

        public SerieStarted build()              { return new SerieStarted(this); }
    }
}
