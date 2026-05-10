package com.kiniot.uflex.api.organization.domain.model.events;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class ClinicSuspendedEvent extends ApplicationEvent {

    private final ClinicId clinicId;
    private final String reason;
    private final Instant occurredOn;

    public ClinicSuspendedEvent(Object source, ClinicId clinicId, String reason) {
        super(source);
        this.clinicId = clinicId;
        this.reason = reason;
        this.occurredOn = Instant.now();
    }
}