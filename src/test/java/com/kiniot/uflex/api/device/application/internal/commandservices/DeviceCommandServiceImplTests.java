package com.kiniot.uflex.api.device.application.internal.commandservices;

import com.kiniot.uflex.api.device.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.device.domain.model.aggregates.Device;
import com.kiniot.uflex.api.device.domain.model.commands.RegisterDeviceCommand;
import com.kiniot.uflex.api.device.domain.model.commands.AssignDeviceToPatientCommand;
import com.kiniot.uflex.api.device.domain.model.commands.AssignStockToClinicCommand;
import com.kiniot.uflex.api.device.domain.exceptions.DeviceAssignmentNotAllowedException;
import com.kiniot.uflex.api.device.domain.exceptions.DeviceNotInStockException;
import com.kiniot.uflex.api.device.domain.model.valueobjects.AdvertisedName;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceModel;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceStatus;
import com.kiniot.uflex.api.device.domain.model.valueobjects.FirmwareVersion;
import com.kiniot.uflex.api.device.domain.model.valueobjects.MacAddress;
import com.kiniot.uflex.api.device.domain.model.valueobjects.SerialNumber;
import com.kiniot.uflex.api.device.infrastructure.persistence.jpa.repositories.DeviceRepository;
import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeviceCommandServiceImplTests {

    private DeviceRepository deviceRepository;
    private ExternalIamService externalIamService;
    private ExternalOrganizationService externalOrganizationService;
    private DeviceCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        deviceRepository = mock(DeviceRepository.class);
        externalIamService = mock(ExternalIamService.class);
        externalOrganizationService = mock(ExternalOrganizationService.class);
        service = new DeviceCommandServiceImpl(deviceRepository, externalIamService, externalOrganizationService);
    }

    @Test
    void assignDeviceValidatesPatientAgainstAuthenticatedClinic() {
        var clinicId = new ClinicId(UUID.randomUUID());
        var patientId = new PatientId();
        var device = availableDevice(clinicId);
        var command = new AssignDeviceToPatientCommand(device.getId(), patientId);
        when(deviceRepository.findById(command.deviceId())).thenReturn(Optional.of(device));
        when(externalIamService.fetchCurrentClinicId()).thenReturn(Optional.of(clinicId));
        when(externalOrganizationService.patientBelongsToClinic(
                patientId.patientId().toString(),
                clinicId.id().toString()
        )).thenReturn(true);

        service.handle(command);

        assertEquals(DeviceStatus.ASSIGNED, device.getStatus());
        assertEquals(patientId, device.getCurrentPatientId());
        verify(externalOrganizationService).patientBelongsToClinic(
                patientId.patientId().toString(),
                clinicId.id().toString()
        );
        verify(deviceRepository).save(device);
    }

    @Test
    void assignDeviceRejectsRequestWithoutAuthenticatedClinic() {
        var device = availableDevice(new ClinicId(UUID.randomUUID()));
        var command = new AssignDeviceToPatientCommand(device.getId(), new PatientId());
        when(deviceRepository.findById(command.deviceId())).thenReturn(Optional.of(device));
        when(externalIamService.fetchCurrentClinicId()).thenReturn(Optional.empty());

        assertThrows(AuthenticatedUserClinicNotFoundException.class, () -> service.handle(command));
    }

    @Test
    void assignedDeviceCannotBeAssignedTwice() {
        var device = availableDevice(new ClinicId(UUID.randomUUID()));
        device.assignToPatient(new PatientId());

        assertThrows(DeviceAssignmentNotAllowedException.class, () -> device.assignToPatient(new PatientId()));
    }

    @Test
    void recordTelemetryUpdatesLastSeenAt() {
        var device = availableDevice(new ClinicId(UUID.randomUUID()));

        device.recordTelemetry(75);

        assertEquals(75, device.getBatteryLevel().percentage());
        assertNotNull(device.getLastSeenAt());
        assertEquals(false, device.isOffline());
    }

    @Test
    void registerDeviceCommandAcceptsNullAdvertisedName() {
        var command = new RegisterDeviceCommand(
                new SerialNumber("UFLEX-DEV-0012"),
                new MacAddress("AA:BB:CC:DD:EE:01"),
                new FirmwareVersion("1.0.0"),
                new DeviceModel("UFlex Tracker Pro"),
                null
        );

        assertEquals("UFLEX-DEV-0012", command.serialNumber().value());
        assertNull(command.advertisedName());
    }

    @Test
    void registerDeviceCommandAcceptsAdvertisedName() {
        var command = new RegisterDeviceCommand(
                new SerialNumber("UFLEX-DEV-0012"),
                new MacAddress("AA:BB:CC:DD:EE:01"),
                new FirmwareVersion("1.0.0"),
                new DeviceModel("UFlex Tracker Pro"),
                new AdvertisedName("UFLEX-DEV-0012")
        );

        assertEquals("UFLEX-DEV-0012", command.advertisedName().value());
    }

    @Test
    void assignToClinicMovesStockDeviceToAvailable() {
        var device = stockDevice("KIT-STOCK-1");
        var clinicId = new ClinicId(UUID.randomUUID());

        device.assignToClinic(clinicId);

        assertEquals(DeviceStatus.AVAILABLE, device.getStatus());
        assertEquals(clinicId, device.getClinicId());
    }

    @Test
    void assignToClinicRejectsDeviceNotInStock() {
        var device = availableDevice(new ClinicId(UUID.randomUUID()));

        assertThrows(DeviceNotInStockException.class,
                () -> device.assignToClinic(new ClinicId(UUID.randomUUID())));
    }

    @Test
    void assignStockAssignsOnlyTheShortfallCappedByStock() {
        var clinicId = new ClinicId(UUID.randomUUID());
        when(deviceRepository.countByClinicIdAndStatusNot(clinicId, DeviceStatus.RETIRED)).thenReturn(1L);
        var stock = new ArrayList<>(List.of(stockDevice("KIT-1"), stockDevice("KIT-2"), stockDevice("KIT-3")));
        when(deviceRepository.findAllInStockByStatus(DeviceStatus.IN_STOCK)).thenReturn(stock);

        int assigned = service.handle(new AssignStockToClinicCommand(clinicId, 3));

        assertEquals(2, assigned);
        assertEquals(DeviceStatus.AVAILABLE, stock.get(0).getStatus());
        assertEquals(clinicId, stock.get(0).getClinicId());
        assertEquals(DeviceStatus.AVAILABLE, stock.get(1).getStatus());
        assertEquals(DeviceStatus.IN_STOCK, stock.get(2).getStatus());
        verify(deviceRepository, times(2)).save(any(Device.class));
    }

    @Test
    void assignStockIsIdempotentWhenClinicAlreadyOwnsEnough() {
        var clinicId = new ClinicId(UUID.randomUUID());
        when(deviceRepository.countByClinicIdAndStatusNot(clinicId, DeviceStatus.RETIRED)).thenReturn(3L);

        int assigned = service.handle(new AssignStockToClinicCommand(clinicId, 3));

        assertEquals(0, assigned);
        verify(deviceRepository, never()).findAllInStockByStatus(any());
    }

    @Test
    void assignStockLeavesShortfallPendingWhenStockInsufficient() {
        var clinicId = new ClinicId(UUID.randomUUID());
        when(deviceRepository.countByClinicIdAndStatusNot(clinicId, DeviceStatus.RETIRED)).thenReturn(0L);
        when(deviceRepository.findAllInStockByStatus(DeviceStatus.IN_STOCK))
                .thenReturn(new ArrayList<>(List.of(stockDevice("KIT-1"))));

        int assigned = service.handle(new AssignStockToClinicCommand(clinicId, 3));

        assertEquals(1, assigned);
    }

    private Device stockDevice(String serial) {
        return Device.registerToStock(
                new SerialNumber(serial),
                new MacAddress("AA:BB:CC:DD:EE:FF"),
                new FirmwareVersion("1.0.0"),
                new DeviceModel("uFlex Tracker"),
                new AdvertisedName(serial)
        );
    }

    private Device availableDevice(ClinicId clinicId) {
        return new Device(
                new SerialNumber("KIT-0001"),
                new MacAddress("AA:BB:CC:DD:EE:FF"),
                new FirmwareVersion("1.0.0"),
                new DeviceModel("UFLEX-ROM"),
                new AdvertisedName("KIT-0001"),
                clinicId
        );
    }
}
