package com.kiniot.uflex.api.device.domain.model.commands;

import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceModel;
import com.kiniot.uflex.api.device.domain.model.valueobjects.FirmwareVersion;
import com.kiniot.uflex.api.device.domain.model.valueobjects.MacAddress;
import com.kiniot.uflex.api.device.domain.model.valueobjects.SerialNumber;

public record RegisterDeviceCommand(
        SerialNumber serialNumber,
        MacAddress macAddress,
        FirmwareVersion firmwareVersion,
        DeviceModel model
) {
    public RegisterDeviceCommand {
        if (serialNumber == null) {
            throw new IllegalArgumentException("Serial number cannot be null");
        }
        if (macAddress == null) {
            throw new IllegalArgumentException("MAC address cannot be null");
        }
    }
}