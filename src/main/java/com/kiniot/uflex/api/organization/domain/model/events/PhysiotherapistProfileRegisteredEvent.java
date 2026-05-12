package com.kiniot.uflex.api.organization.domain.model.events;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhysiotherapistId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ClinicId;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class PhysiotherapistProfileRegisteredEvent extends ApplicationEvent {

    private final PhysiotherapistId physiotherapistId;
    private final UserId userId;
    private final ClinicId clinicId;
    private final Instant occurredOn;

    public PhysiotherapistProfileRegisteredEvent(Object source, PhysiotherapistId physiotherapistId, UserId userId, ClinicId clinicId) {
        super(source);
        this.physiotherapistId = physiotherapistId;
        this.userId = userId;
        this.clinicId = clinicId;
        this.occurredOn = Instant.now();
    }
}