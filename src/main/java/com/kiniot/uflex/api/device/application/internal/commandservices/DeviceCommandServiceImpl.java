package com.kiniot.uflex.api.device.application.internal.commandservices;

import com.kiniot.uflex.api.device.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.device.domain.exceptions.DeviceAlreadyRegisteredException;
import com.kiniot.uflex.api.device.domain.exceptions.DeviceClinicMismatchException;
import com.kiniot.uflex.api.device.domain.exceptions.DeviceNotFoundException;
import com.kiniot.uflex.api.device.domain.model.aggregates.Device;
import com.kiniot.uflex.api.device.domain.model.commands.*;
import com.kiniot.uflex.api.device.domain.model.valueobjects.*;
import com.kiniot.uflex.api.device.domain.services.DeviceCommandService;
import com.kiniot.uflex.api.device.infrastructure.persistence.jpa.repositories.DeviceRepository;
import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceCommandServiceImpl implements DeviceCommandService {

    private final DeviceRepository deviceRepository;
    private final ExternalIamService externalIamService;
    private final ExternalOrganizationService externalOrganizationService;

    public DeviceCommandServiceImpl(
            DeviceRepository deviceRepository,
            ExternalIamService externalIamService,
            ExternalOrganizationService externalOrganizationService
    ) {
        this.deviceRepository = deviceRepository;
        this.externalIamService = externalIamService;
        this.externalOrganizationService = externalOrganizationService;
    }

    @Override
    @Transactional
    public Device handle(RegisterDeviceCommand command, ClinicId clinicId) {
        if (deviceRepository.existsBySerialNumber(command.serialNumber())) {
            throw new DeviceAlreadyRegisteredException(command.serialNumber().value());
        }
        // Identity contract: advertisedName must equal the serial number (it is what the
        // firmware actually advertises over BLE). Default it to the serial when omitted so
        // discovery-by-name always matches the registry (see device-identity-contract).
        var advertisedName = (command.advertisedName() == null || command.advertisedName().value() == null)
                ? new AdvertisedName(command.serialNumber().value())
                : command.advertisedName();
        var device = new Device(
                command.serialNumber(),
                command.macAddress(),
                command.firmwareVersion(),
                command.model(),
                advertisedName,
                clinicId
        );
        return deviceRepository.save(device);
    }

    @Override
    @Transactional
    public void handle(AssignDeviceToPatientCommand command) {
        var device = getDeviceOrThrow(command.deviceId());
        var clinicIdStr = externalIamService.fetchCurrentClinicId()
                .map(clinicId -> clinicId.id().toString())
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        externalOrganizationService.patientBelongsToClinic(
                command.patientId().patientId().toString(),
                clinicIdStr
        );
        device.assignToPatient(command.patientId());
        deviceRepository.save(device);
    }

    @Override
    @Transactional
    public void handle(ReturnDeviceCommand command) {
        var device = getDeviceOrThrow(command.deviceId());
        device.returnFromPatient();
        deviceRepository.save(device);
    }

    @Override
    @Transactional
    public void handle(UpdateDeviceTelemetryCommand command) {
        var device = getDeviceOrThrow(command.deviceId());
        device.recordTelemetry(command.batteryLevel());
        deviceRepository.save(device);
    }

    @Override
    @Transactional
    public void handle(RegisterSuccessfulCalibrationCommand command) {
        var device = getDeviceOrThrow(command.deviceId());
        device.registerSuccessfulCalibration();
        deviceRepository.save(device);
    }

    @Override
    @Transactional
    public void handle(MarkCalibrationAsInvalidCommand command) {
        var device = getDeviceOrThrow(command.deviceId());
        device.markCalibrationAsInvalid();
        deviceRepository.save(device);
    }

    @Override
    @Transactional
    public void handle(UpdateDeviceStatusCommand command) {
        var device = getDeviceOrThrow(command.deviceId());
        device.updateStatus(command.status());
        deviceRepository.save(device);
    }

    @Override
    @Transactional
    public void handle(DeleteDeviceCommand command) {
        var device = getDeviceOrThrow(command.deviceId());
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        if (!device.getClinicId().equals(clinicId)) {
            throw new DeviceClinicMismatchException();
        }
        deviceRepository.delete(device);
    }

    private Device getDeviceOrThrow(DeviceId deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException(deviceId.id().toString()));
    }
}
