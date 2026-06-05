package com.kiniot.uflex.api.device.application.internal.commandservices;

import com.kiniot.uflex.api.device.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.device.domain.model.aggregates.Device;
import com.kiniot.uflex.api.device.domain.model.commands.*;
import com.kiniot.uflex.api.device.domain.model.valueobjects.*;
import com.kiniot.uflex.api.device.domain.services.DeviceCommandService;
import com.kiniot.uflex.api.device.infrastructure.persistence.jpa.repositories.DeviceRepository;
import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
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
            throw new IllegalArgumentException("Device with serial number already exists");
        }
        var device = new Device(
                command.serialNumber(),
                command.macAddress(),
                command.firmwareVersion(),
                command.model(),
                clinicId
        );
        return deviceRepository.save(device);
    }

    @Override
    @Transactional
    public void handle(AssignDeviceToPatientCommand command) {
        var device = getDeviceOrThrow(command.serialNumber());
        var clinicIdStr = externalIamService.fetchCurrentClinicId()
                .map(clinicId -> clinicId.id().toString())
                .orElseThrow(() -> new IllegalStateException("Clinic not found"));
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
        var device = getDeviceOrThrow(command.serialNumber());
        device.returnFromPatient();
        deviceRepository.save(device);
    }

    @Override
    @Transactional
    public void handle(UpdateDeviceTelemetryCommand command) {
        var device = getDeviceOrThrow(command.serialNumber());
        device.recordTelemetry(command.batteryLevel());
        deviceRepository.save(device);
    }

    @Override
    @Transactional
    public void handle(RegisterSuccessfulCalibrationCommand command) {
        var device = getDeviceOrThrow(command.serialNumber());
        device.registerSuccessfulCalibration();
        deviceRepository.save(device);
    }

    @Override
    @Transactional
    public void handle(MarkCalibrationAsInvalidCommand command) {
        var device = getDeviceOrThrow(command.serialNumber());
        device.markCalibrationAsInvalid();
        deviceRepository.save(device);
    }

    @Override
    @Transactional
    public void handle(UpdateDeviceStatusCommand command) {
        var device = getDeviceOrThrow(command.serialNumber());
        device.updateStatus(command.status());
        deviceRepository.save(device);
    }

    private Device getDeviceOrThrow(SerialNumber serialNumber) {
        return deviceRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new IllegalStateException("Device not found with serial number: " + serialNumber.value()));
    }
}