package com.kiniot.uflex.api.device.application.internal.commandservices;

import com.kiniot.uflex.api.device.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.device.domain.exceptions.DeviceAlreadyRegisteredException;
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

import java.util.List;

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
    public Device handle(RegisterDeviceCommand command) {
        if (deviceRepository.existsBySerialNumber(command.serialNumber())) {
            throw new DeviceAlreadyRegisteredException(command.serialNumber().value());
        }
        // Identity contract: advertisedName must equal the serial number (it is what the
        // firmware actually advertises over BLE). Default it to the serial when omitted so
        // discovery-by-name always matches the registry (see device-identity-contract).
        var advertisedName = (command.advertisedName() == null || command.advertisedName().value() == null)
                ? new AdvertisedName(command.serialNumber().value())
                : command.advertisedName();
        // Devices are registered into the global, clinic-less inventory (stock). They are
        // assigned to a clinic later, when that clinic's subscription is activated.
        var device = Device.registerToStock(
                command.serialNumber(),
                command.macAddress(),
                command.firmwareVersion(),
                command.model(),
                advertisedName
        );
        return deviceRepository.save(device);
    }

    @Override
    @Transactional
    public int handle(AssignStockToClinicCommand command) {
        var clinicId = command.clinicId();
        long alreadyOwned = deviceRepository.countByClinicIdAndStatusNot(clinicId, DeviceStatus.RETIRED);
        int needed = (int) Math.max(0, command.requestedTotalKits() - alreadyOwned);
        if (needed == 0) return 0;
        var stock = deviceRepository.findAllInStockByStatus(DeviceStatus.IN_STOCK);
        int toAssign = Math.min(needed, stock.size());
        for (int i = 0; i < toAssign; i++) {
            var device = stock.get(i);
            device.assignToClinic(clinicId);
            deviceRepository.save(device);
        }
        return toAssign;
    }

    @Override
    @Transactional
    public int handle(SeedDevicesCommand command) {
        record SeedDevice(String serial, String mac, String firmware, String model) {}
        var seedDevices = List.of(
                new SeedDevice("uflex-kit-001", "AA:BB:CC:DD:EE:01", "1.0.0", "uFlex Tracker"),
                new SeedDevice("uflex-kit-002", "AA:BB:CC:DD:EE:02", "1.0.0", "uFlex Tracker"),
                new SeedDevice("uflex-kit-003", "AA:BB:CC:DD:EE:03", "1.0.0", "uFlex Tracker"),
                new SeedDevice("uflex-kit-004", "AA:BB:CC:DD:EE:04", "1.0.0", "uFlex Tracker"),
                new SeedDevice("uflex-kit-005", "AA:BB:CC:DD:EE:05", "1.0.0", "uFlex Tracker")
        );
        int created = 0;
        for (var seed : seedDevices) {
            var serialNumber = new SerialNumber(seed.serial());
            if (deviceRepository.existsBySerialNumber(serialNumber)) continue;
            var device = Device.registerToStock(
                    serialNumber,
                    new MacAddress(seed.mac()),
                    new FirmwareVersion(seed.firmware()),
                    new DeviceModel(seed.model()),
                    new AdvertisedName(seed.serial())
            );
            deviceRepository.save(device);
            created++;
        }
        return created;
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
        // Device lifecycle (retire/delete) is a platform/operations concern handled by
        // ROLE_DEVELOPER, not scoped to a single clinic.
        var device = getDeviceOrThrow(command.deviceId());
        deviceRepository.delete(device);
    }

    private Device getDeviceOrThrow(DeviceId deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException(deviceId.id().toString()));
    }
}
