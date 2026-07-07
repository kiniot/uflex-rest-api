package com.kiniot.uflex.api.device.application.internal.queryservices;

import com.kiniot.uflex.api.device.domain.model.aggregates.Device;
import com.kiniot.uflex.api.device.domain.model.queries.*;
import com.kiniot.uflex.api.device.domain.model.valueobjects.CalibrationStatus;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceStatus;
import com.kiniot.uflex.api.device.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.device.application.internal.outboundservices.acl.ExternalSubscriptionService;
import com.kiniot.uflex.api.device.domain.services.DeviceQueryService;
import com.kiniot.uflex.api.device.infrastructure.persistence.jpa.repositories.DeviceRepository;
import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DeviceQueryServiceImpl implements DeviceQueryService {

    private final DeviceRepository deviceRepository;
    private final ExternalIamService externalIamService;
    private final ExternalSubscriptionService externalSubscriptionService;
    private final ExternalOrganizationService externalOrganizationService;

    public DeviceQueryServiceImpl(
            DeviceRepository deviceRepository,
            ExternalIamService externalIamService,
            ExternalSubscriptionService externalSubscriptionService,
            ExternalOrganizationService externalOrganizationService
    ) {
        this.deviceRepository = deviceRepository;
        this.externalIamService = externalIamService;
        this.externalSubscriptionService = externalSubscriptionService;
        this.externalOrganizationService = externalOrganizationService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Device> handle(GetDeviceByIdQuery query) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        return deviceRepository.findById(query.deviceId())
                .filter(device -> device.getClinicId().equals(clinicId));
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

        int owned = (int) devices.stream()
                .filter(d -> d.getStatus() != DeviceStatus.RETIRED)
                .count();
        int requestedKits = externalSubscriptionService.getRequestedTotalKits(query.clinicId());
        int pendingKits = Math.max(0, requestedKits - owned);

        return new ClinicFleetMetrics(total, available, assigned, inMaintenance, lowBattery, offline, requestedKits, pendingKits);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Device> handle(GetStockDevicesQuery query) {
        return deviceRepository.findAllInStock();
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalDeviceOverview handle(GetGlobalDeviceOverviewQuery query) {
        int total = (int) deviceRepository.count();
        int inStock = (int) deviceRepository.countInStock();
        int available = (int) deviceRepository.countByStatus(DeviceStatus.AVAILABLE);
        int assigned = (int) deviceRepository.countByStatus(DeviceStatus.ASSIGNED);
        int inMaintenance = (int) deviceRepository.countByStatus(DeviceStatus.IN_MAINTENANCE);
        int retired = (int) deviceRepository.countByStatus(DeviceStatus.RETIRED);

        var grouped = deviceRepository.countGroupedByClinicId();
        var clinicIds = grouped.stream()
                .map(row -> row[0].toString())
                .toList();
        var clinicNames = externalOrganizationService.getClinicNames(clinicIds);
        var perClinic = grouped.stream()
                .map(row -> {
                    var clinicId = row[0].toString();
                    var count = ((Number) row[1]).intValue();
                    return new ClinicDeviceCount(clinicId, clinicNames.getOrDefault(clinicId, "Unknown clinic"), count);
                })
                .sorted((a, b) -> Integer.compare(b.count(), a.count()))
                .toList();

        return new GlobalDeviceOverview(total, inStock, available, assigned, inMaintenance, retired, perClinic.size(), perClinic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FulfillmentRow> handle(GetFulfillmentQueueQuery query) {
        var entitlements = externalSubscriptionService.getCurrentEntitlements();
        if (entitlements.isEmpty()) return List.of();

        var ownedByClinic = deviceRepository.countGroupedByClinicId().stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> row[0].toString(),
                        row -> ((Number) row[1]).intValue()));
        var clinicNames = externalOrganizationService.getClinicNames(
                entitlements.stream().map(e -> e.clinicId()).toList());

        return entitlements.stream()
                .map(e -> {
                    int owned = ownedByClinic.getOrDefault(e.clinicId(), 0);
                    int pending = Math.max(0, e.requestedTotalKits() - owned);
                    return new FulfillmentRow(
                            e.clinicId(),
                            clinicNames.getOrDefault(e.clinicId(), "Unknown clinic"),
                            e.requestedTotalKits(),
                            owned,
                            pending);
                })
                .sorted((a, b) -> Integer.compare(b.pending(), a.pending()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FleetHealth handle(GetFleetHealthQuery query) {
        var cutoff = LocalDateTime.now().minusMinutes(15);
        var offline = deviceRepository.findOwnedOffline(DeviceStatus.RETIRED, cutoff);
        var lowBattery = deviceRepository.findOwnedLowBattery(DeviceStatus.RETIRED, 20);
        var needsCalibration = deviceRepository.findOwnedNeedingCalibration(
                DeviceStatus.RETIRED, CalibrationStatus.NEEDS_CALIBRATION);

        var clinicIds = java.util.stream.Stream.of(offline, lowBattery, needsCalibration)
                .flatMap(List::stream)
                .map(d -> d.getClinicId().id().toString())
                .distinct()
                .toList();
        var names = externalOrganizationService.getClinicNames(clinicIds);

        return new FleetHealth(
                offline.size(), lowBattery.size(), needsCalibration.size(),
                toHealthRows(offline, names),
                toHealthRows(lowBattery, names),
                toHealthRows(needsCalibration, names));
    }

    private List<DeviceHealthRow> toHealthRows(List<Device> devices, Map<String, String> names) {
        return devices.stream().map(d -> {
            var clinicId = d.getClinicId().id().toString();
            return new DeviceHealthRow(
                    d.getId().id().toString(),
                    d.getSerialNumber().value(),
                    clinicId,
                    names.getOrDefault(clinicId, "Unknown clinic"),
                    d.getStatus(),
                    d.getBatteryLevel() != null ? d.getBatteryLevel().percentage() : null,
                    d.getLastSeenAt());
        }).toList();
    }
}
