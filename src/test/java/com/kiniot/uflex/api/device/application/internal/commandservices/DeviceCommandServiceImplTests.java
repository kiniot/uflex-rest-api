package com.kiniot.uflex.api.device.application.internal.commandservices;

import com.kiniot.uflex.api.device.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.device.domain.model.aggregates.Device;
import com.kiniot.uflex.api.device.domain.model.commands.AssignDeviceToPatientCommand;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceModel;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceStatus;
import com.kiniot.uflex.api.device.domain.model.valueobjects.FirmwareVersion;
import com.kiniot.uflex.api.device.domain.model.valueobjects.MacAddress;
import com.kiniot.uflex.api.device.domain.model.valueobjects.SerialNumber;
import com.kiniot.uflex.api.device.infrastructure.persistence.jpa.repositories.DeviceRepository;
import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
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
        var command = new AssignDeviceToPatientCommand(device.getSerialNumber(), patientId);
        when(deviceRepository.findBySerialNumber(command.serialNumber())).thenReturn(Optional.of(device));
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
        var command = new AssignDeviceToPatientCommand(device.getSerialNumber(), new PatientId());
        when(deviceRepository.findBySerialNumber(command.serialNumber())).thenReturn(Optional.of(device));
        when(externalIamService.fetchCurrentClinicId()).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> service.handle(command));
    }

    @Test
    void assignedDeviceCannotBeAssignedTwice() {
        var device = availableDevice(new ClinicId(UUID.randomUUID()));
        device.assignToPatient(new PatientId());

        assertThrows(IllegalStateException.class, () -> device.assignToPatient(new PatientId()));
    }

    private Device availableDevice(ClinicId clinicId) {
        return new Device(
                new SerialNumber("KIT-0001"),
                new MacAddress("AA:BB:CC:DD:EE:FF"),
                new FirmwareVersion("1.0.0"),
                new DeviceModel("UFLEX-ROM"),
                clinicId
        );
    }
}
