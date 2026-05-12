package com.kiniot.uflex.api.organization.domain.model.events;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.PatientId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ClinicId;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class PatientProfileRegisteredEvent extends ApplicationEvent {

    private final PatientId patientId;
    private final UserId userId;
    private final ClinicId clinicId;
    private final Instant occurredOn;

    public PatientProfileRegisteredEvent(Object source, PatientId patientId, UserId userId, ClinicId clinicId) {
        super(source);
        this.patientId = patientId;
        this.userId = userId;
        this.clinicId = clinicId;
        this.occurredOn = Instant.now();
    }
}