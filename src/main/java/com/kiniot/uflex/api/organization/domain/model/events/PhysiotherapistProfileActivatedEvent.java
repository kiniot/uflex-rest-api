package com.kiniot.uflex.api.organization.domain.model.events;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class PhysiotherapistProfileActivatedEvent extends ApplicationEvent {

    private final PhysiotherapistId physiotherapistId;
    private final UserId userId;
    private final ClinicId clinicId;
    private final Instant occurredOn;

    public PhysiotherapistProfileActivatedEvent(Object source, PhysiotherapistId physiotherapistId, UserId userId, ClinicId clinicId) {
        super(source);
        this.physiotherapistId = physiotherapistId;
        this.userId = userId;
        this.clinicId = clinicId;
        this.occurredOn = Instant.now();
    }
}
