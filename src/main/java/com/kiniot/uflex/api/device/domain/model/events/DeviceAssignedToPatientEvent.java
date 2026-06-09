package com.kiniot.uflex.api.device.domain.model.events;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DeviceAssignedToPatientEvent extends ApplicationEvent {

    private final String serialNumber;
    private final PatientId patientId;

    public DeviceAssignedToPatientEvent(Object source, String serialNumber, PatientId patientId) {
        super(source);
        this.serialNumber = serialNumber;
        this.patientId = patientId;
    }
}