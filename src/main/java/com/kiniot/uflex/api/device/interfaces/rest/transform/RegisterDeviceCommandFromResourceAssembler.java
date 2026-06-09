package com.kiniot.uflex.api.device.interfaces.rest.transform;

import com.kiniot.uflex.api.device.domain.model.commands.RegisterDeviceCommand;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceModel;
import com.kiniot.uflex.api.device.domain.model.valueobjects.FirmwareVersion;
import com.kiniot.uflex.api.device.domain.model.valueobjects.MacAddress;
import com.kiniot.uflex.api.device.domain.model.valueobjects.SerialNumber;
import com.kiniot.uflex.api.device.interfaces.rest.resources.RegisterDeviceResource;

public class RegisterDeviceCommandFromResourceAssembler {

    public static RegisterDeviceCommand toCommandFromResource(RegisterDeviceResource resource) {
        return new RegisterDeviceCommand(
                resource.serialNumber() != null
                        ? new SerialNumber(resource.serialNumber())
                        : null,
                resource.macAddress() != null
                        ? new MacAddress(resource.macAddress())
                        : null,
                resource.firmwareVersion() != null
                        ? new FirmwareVersion(resource.firmwareVersion())
                        : null,
                resource.model() != null
                        ? new DeviceModel(resource.model())
                        : null
        );
    }
}