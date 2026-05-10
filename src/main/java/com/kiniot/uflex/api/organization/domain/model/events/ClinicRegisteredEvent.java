package com.kiniot.uflex.api.organization.domain.model.events;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class ClinicRegisteredEvent extends ApplicationEvent {

    private final ClinicId clinicId;
    private final String legalName;
    private final String taxId;
    private final String createdBy;
    private final Instant occurredOn;

    public ClinicRegisteredEvent(Object source, ClinicId clinicId, String legalName, String taxId, String createdBy) {
        super(source);
        this.clinicId = clinicId;
        this.legalName = legalName;
        this.taxId = taxId;
        this.createdBy = createdBy;
        this.occurredOn = Instant.now();
    }
}