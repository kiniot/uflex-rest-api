package com.kiniot.uflex.api.device.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Raised when a device is added to the global, clinic-less inventory (stock).
 * It carries no clinic because a stock device has no owner yet.
 */
@Getter
public class DeviceAddedToStockEvent extends ApplicationEvent {

    private final String serialNumber;

    public DeviceAddedToStockEvent(Object source, String serialNumber) {
        super(source);
        this.serialNumber = serialNumber;
    }
}
