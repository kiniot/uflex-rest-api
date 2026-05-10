package com.kiniot.uflex.api.organization.domain.model.events;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class ClinicActivatedEvent extends ApplicationEvent {

    private final ClinicId clinicId;
    private final Instant occurredOn;

    public ClinicActivatedEvent(Object source, ClinicId clinicId) {
        super(source);
        this.clinicId = clinicId;
        this.occurredOn = Instant.now();
    }
}