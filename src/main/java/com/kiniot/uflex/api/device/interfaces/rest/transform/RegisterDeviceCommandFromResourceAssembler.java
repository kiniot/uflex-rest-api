package com.kiniot.uflex.api.device.interfaces.rest.transform;

import com.kiniot.uflex.api.device.domain.model.commands.RegisterDeviceCommand;
import com.kiniot.uflex.api.device.interfaces.rest.resources.RegisterDeviceResource;

public class RegisterDeviceCommandFromResourceAssembler {

    public static RegisterDeviceCommand toCommandFromResource(RegisterDeviceResource resource) {
        return new RegisterDeviceCommand(
                resource.serialNumber() != null
                        ? new com.kiniot.uflex.api.device.domain.model.valueobjects.SerialNumber(resource.serialNumber())
                        : null,
                resource.macAddress() != null
                        ? new com.kiniot.uflex.api.device.domain.model.valueobjects.MacAddress(resource.macAddress())
                        : null,
                resource.firmwareVersion() != null
                        ? new com.kiniot.uflex.api.device.domain.model.valueobjects.FirmwareVersion(resource.firmwareVersion())
                        : null,
                resource.model() != null
                        ? new com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceModel(resource.model())
                        : null
        );
    }
}