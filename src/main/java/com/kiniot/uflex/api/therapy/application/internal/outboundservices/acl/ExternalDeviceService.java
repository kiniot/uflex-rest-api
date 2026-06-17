package com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.device.interfaces.acl.DeviceContextFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("therapyExternalDeviceService")
public class ExternalDeviceService {

    private final DeviceContextFacade deviceContextFacade;

    public ExternalDeviceService(DeviceContextFacade deviceContextFacade) {
        this.deviceContextFacade = deviceContextFacade;
    }

    public boolean isDeviceAssignedToPatient(String deviceIdentifier, String clinicId, String patientId) {
        log.debug("Validating device assignment: deviceIdentifier={}, patientId={}", deviceIdentifier, patientId);
        return deviceContextFacade.isDeviceAssignedToPatient(deviceIdentifier, clinicId, patientId);
    }
}
