package com.kiniot.uflex.api.device.application.internal.queryservices;

import com.kiniot.uflex.api.device.domain.model.aggregates.Device;
import com.kiniot.uflex.api.device.domain.model.queries.*;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceStatus;
import com.kiniot.uflex.api.device.domain.services.DeviceQueryService;
import com.kiniot.uflex.api.device.infrastructure.persistence.jpa.repositories.DeviceRepository;
import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceQueryServiceImpl implements DeviceQueryService {

    private final DeviceRepository deviceRepository;
    private final ExternalIamService externalIamService;

    public DeviceQueryServiceImpl(
            DeviceRepository deviceRepository,
            ExternalIamService externalIamService
    ) {
        this.deviceRepository = deviceRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Device> handle(GetDeviceBySerialNumberQuery query) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        return deviceRepository.findBySerialNumber(query.serialNumber())
                .filter(device -> device.getClinicId().equals(clinicId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Device> handle(GetMyAssignedDeviceQuery query) {
        return deviceRepository.findByCurrentPatientId(query.patientId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Device> handle(GetAllDevicesByClinicIdQuery query) {
        if (query.status() != null) {
            return deviceRepository.findAllByClinicIdAndStatus(query.clinicId(), query.status());
        }
        return deviceRepository.findAllByClinicId(query.clinicId());
    }

    @Override
    @Transactional(readOnly = true)
    public ClinicFleetMetrics handle(GetClinicFleetMetricsQuery query) {
        var devices = deviceRepository.findAllByClinicId(query.clinicId());

        int total = devices.size();
        int available = (int) devices.stream()
                .filter(d -> d.getStatus() == DeviceStatus.AVAILABLE)
                .count();
        int assigned = (int) devices.stream()
                .filter(d -> d.getStatus() == DeviceStatus.ASSIGNED)
                .count();
        int inMaintenance = (int) devices.stream()
                .filter(d -> d.getStatus() == DeviceStatus.IN_MAINTENANCE)
                .count();
        int lowBattery = (int) devices.stream()
                .filter(d -> d.getBatteryLevel() != null && d.getBatteryLevel().isLow())
                .count();
        int offline = (int) devices.stream()
                .filter(Device::isOffline)
                .count();

        return new ClinicFleetMetrics(total, available, assigned, inMaintenance, lowBattery, offline);
    }
}