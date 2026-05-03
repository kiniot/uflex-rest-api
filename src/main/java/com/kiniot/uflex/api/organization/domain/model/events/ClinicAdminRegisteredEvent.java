package com.kiniot.uflex.api.organization.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ClinicAdminRegisteredEvent extends ApplicationEvent {

    private final String userId;
    private final String clinicId;

    public ClinicAdminRegisteredEvent(Object source, String userId, String clinicId) {
        super(source);
        this.userId = userId;
        this.clinicId = clinicId;
    }
}
