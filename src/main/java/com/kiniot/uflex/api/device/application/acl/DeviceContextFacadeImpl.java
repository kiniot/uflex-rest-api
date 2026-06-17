package com.kiniot.uflex.api.device.application.acl;

import com.kiniot.uflex.api.device.domain.model.aggregates.Device;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceId;
import com.kiniot.uflex.api.device.domain.model.valueobjects.SerialNumber;
import com.kiniot.uflex.api.device.infrastructure.persistence.jpa.repositories.DeviceRepository;
import com.kiniot.uflex.api.device.interfaces.acl.DeviceContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class DeviceContextFacadeImpl implements DeviceContextFacade {

    private final DeviceRepository deviceRepository;

    public DeviceContextFacadeImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDeviceAssignedToPatient(String deviceIdentifier, String clinicId, String patientId) {
        return findDevice(deviceIdentifier)
                .filter(device -> device.getClinicId() != null
                        && device.getClinicId().id().toString().equals(clinicId))
                .filter(device -> device.getCurrentPatientId() != null
                        && device.getCurrentPatientId().patientId().toString().equals(patientId))
                .isPresent();
    }

    private Optional<Device> findDevice(String deviceIdentifier) {
        try {
            return deviceRepository.findById(new DeviceId(UUID.fromString(deviceIdentifier)));
        } catch (IllegalArgumentException e) {
            return deviceRepository.findBySerialNumber(new SerialNumber(deviceIdentifier));
        }
    }
}
