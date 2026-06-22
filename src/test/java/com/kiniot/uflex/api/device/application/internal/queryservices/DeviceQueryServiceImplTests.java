package com.kiniot.uflex.api.device.application.internal.queryservices;

import com.kiniot.uflex.api.device.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.device.application.internal.outboundservices.acl.ExternalSubscriptionService;
import com.kiniot.uflex.api.device.domain.model.aggregates.Device;
import com.kiniot.uflex.api.device.domain.model.queries.GetFleetHealthQuery;
import com.kiniot.uflex.api.device.domain.model.queries.GetFulfillmentQueueQuery;
import com.kiniot.uflex.api.device.domain.model.queries.GetGlobalDeviceOverviewQuery;
import com.kiniot.uflex.api.device.domain.model.valueobjects.AdvertisedName;
import com.kiniot.uflex.api.device.domain.model.valueobjects.CalibrationStatus;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceModel;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceStatus;
import com.kiniot.uflex.api.device.domain.model.valueobjects.FirmwareVersion;
import com.kiniot.uflex.api.device.domain.model.valueobjects.MacAddress;
import com.kiniot.uflex.api.device.domain.model.valueobjects.SerialNumber;
import com.kiniot.uflex.api.device.infrastructure.persistence.jpa.repositories.DeviceRepository;
import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.subscription.interfaces.acl.ClinicEntitlement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeviceQueryServiceImplTests {

    private DeviceRepository deviceRepository;
    private ExternalOrganizationService externalOrganizationService;
    private ExternalSubscriptionService externalSubscriptionService;
    private DeviceQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        deviceRepository = mock(DeviceRepository.class);
        var externalIamService = mock(ExternalIamService.class);
        externalSubscriptionService = mock(ExternalSubscriptionService.class);
        externalOrganizationService = mock(ExternalOrganizationService.class);
        service = new DeviceQueryServiceImpl(
                deviceRepository, externalIamService, externalSubscriptionService, externalOrganizationService);
    }

    @Test
    void globalOverviewAggregatesCountsAndResolvesClinicNamesSortedByCount() {
        var clinicA = UUID.randomUUID();
        var clinicB = UUID.randomUUID();

        when(deviceRepository.count()).thenReturn(10L);
        when(deviceRepository.countInStock()).thenReturn(3L);
        when(deviceRepository.countByStatus(DeviceStatus.AVAILABLE)).thenReturn(4L);
        when(deviceRepository.countByStatus(DeviceStatus.ASSIGNED)).thenReturn(2L);
        when(deviceRepository.countByStatus(DeviceStatus.IN_MAINTENANCE)).thenReturn(1L);
        when(deviceRepository.countByStatus(DeviceStatus.RETIRED)).thenReturn(0L);
        when(deviceRepository.countGroupedByClinicId()).thenReturn(List.of(
                new Object[]{clinicB, 2L},
                new Object[]{clinicA, 4L}
        ));
        when(externalOrganizationService.getClinicNames(any())).thenReturn(Map.of(
                clinicA.toString(), "Clinic A",
                clinicB.toString(), "Clinic B"
        ));

        var overview = service.handle(new GetGlobalDeviceOverviewQuery());

        assertEquals(10, overview.total());
        assertEquals(3, overview.inStock());
        assertEquals(4, overview.available());
        assertEquals(2, overview.assigned());
        assertEquals(1, overview.inMaintenance());
        assertEquals(0, overview.retired());
        assertEquals(2, overview.distinctClinics());
        // sorted by count desc → Clinic A (4) first
        assertEquals(2, overview.perClinic().size());
        assertEquals("Clinic A", overview.perClinic().get(0).clinicName());
        assertEquals(4, overview.perClinic().get(0).count());
        assertEquals("Clinic B", overview.perClinic().get(1).clinicName());
    }

    @Test
    void globalOverviewLabelsUnknownClinicWhenNameMissing() {
        var clinic = UUID.randomUUID();
        when(deviceRepository.count()).thenReturn(1L);
        when(deviceRepository.countInStock()).thenReturn(0L);
        when(deviceRepository.countByStatus(any())).thenReturn(0L);
        when(deviceRepository.countGroupedByClinicId()).thenReturn(List.<Object[]>of(new Object[]{clinic, 1L}));
        when(externalOrganizationService.getClinicNames(any())).thenReturn(Map.of());

        var overview = service.handle(new GetGlobalDeviceOverviewQuery());

        assertEquals("Unknown clinic", overview.perClinic().get(0).clinicName());
    }

    @Test
    void fulfillmentQueueCrossesEntitlementsWithOwnedAndSortsByPending() {
        var clinicA = UUID.randomUUID();
        var clinicB = UUID.randomUUID();
        when(externalSubscriptionService.getCurrentEntitlements()).thenReturn(List.of(
                new ClinicEntitlement(clinicA.toString(), 5),
                new ClinicEntitlement(clinicB.toString(), 10)
        ));
        when(deviceRepository.countGroupedByClinicId()).thenReturn(List.of(
                new Object[]{clinicA, 5L},  // owned 5 → pending 0
                new Object[]{clinicB, 3L}   // owned 3 → pending 7
        ));
        when(externalOrganizationService.getClinicNames(any())).thenReturn(Map.of(
                clinicA.toString(), "Clinic A",
                clinicB.toString(), "Clinic B"
        ));

        var rows = service.handle(new GetFulfillmentQueueQuery());

        assertEquals(2, rows.size());
        // sorted by pending desc → Clinic B (7) first
        assertEquals("Clinic B", rows.get(0).clinicName());
        assertEquals(10, rows.get(0).requested());
        assertEquals(3, rows.get(0).owned());
        assertEquals(7, rows.get(0).pending());
        assertEquals(0, rows.get(1).pending());
    }

    @Test
    void fleetHealthCountsAndMapsAffectedDevices() {
        var clinic = UUID.randomUUID();
        when(deviceRepository.findOwnedOffline(eq(DeviceStatus.RETIRED), any()))
                .thenReturn(List.of(ownedDevice("OFF-1", clinic)));
        when(deviceRepository.findOwnedLowBattery(DeviceStatus.RETIRED, 20))
                .thenReturn(List.of(ownedDevice("LOW-1", clinic)));
        when(deviceRepository.findOwnedNeedingCalibration(DeviceStatus.RETIRED, CalibrationStatus.NEEDS_CALIBRATION))
                .thenReturn(List.of());
        when(externalOrganizationService.getClinicNames(any())).thenReturn(Map.of(clinic.toString(), "Clinic A"));

        var health = service.handle(new GetFleetHealthQuery());

        assertEquals(1, health.offlineCount());
        assertEquals(1, health.lowBatteryCount());
        assertEquals(0, health.needsCalibrationCount());
        assertEquals("OFF-1", health.offline().get(0).serialNumber());
        assertEquals("Clinic A", health.offline().get(0).clinicName());
    }

    private Device ownedDevice(String serial, UUID clinic) {
        var device = Device.registerToStock(
                new SerialNumber(serial),
                new MacAddress("AA:BB:CC:DD:EE:FF"),
                new FirmwareVersion("1.0.0"),
                new DeviceModel("uFlex Tracker"),
                new AdvertisedName(serial));
        device.assignToClinic(new ClinicId(clinic));
        return device;
    }
}
