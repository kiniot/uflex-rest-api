package com.kiniot.uflex.api.device.domain.model.events;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DeviceRegisteredEvent extends ApplicationEvent {

    private final String serialNumber;
    private final ClinicId clinicId;

    public DeviceRegisteredEvent(Object source, String serialNumber, ClinicId clinicId) {
        super(source);
        this.serialNumber = serialNumber;
        this.clinicId = clinicId;
    }
}