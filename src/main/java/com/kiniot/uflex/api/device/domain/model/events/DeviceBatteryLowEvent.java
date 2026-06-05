package com.kiniot.uflex.api.device.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DeviceBatteryLowEvent extends ApplicationEvent {

    private final String serialNumber;
    private final Integer batteryLevel;

    public DeviceBatteryLowEvent(Object source, String serialNumber, Integer batteryLevel) {
        super(source);
        this.serialNumber = serialNumber;
        this.batteryLevel = batteryLevel;
    }
}