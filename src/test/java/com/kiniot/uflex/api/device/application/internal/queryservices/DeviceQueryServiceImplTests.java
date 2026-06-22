package com.kiniot.uflex.api.device.application.internal.queryservices;

import com.kiniot.uflex.api.device.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.device.application.internal.outboundservices.acl.ExternalSubscriptionService;
import com.kiniot.uflex.api.device.domain.model.queries.GetGlobalDeviceOverviewQuery;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceStatus;
import com.kiniot.uflex.api.device.infrastructure.persistence.jpa.repositories.DeviceRepository;
import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeviceQueryServiceImplTests {

    private DeviceRepository deviceRepository;
    private ExternalOrganizationService externalOrganizationService;
    private DeviceQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        deviceRepository = mock(DeviceRepository.class);
        var externalIamService = mock(ExternalIamService.class);
        var externalSubscriptionService = mock(ExternalSubscriptionService.class);
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
}
