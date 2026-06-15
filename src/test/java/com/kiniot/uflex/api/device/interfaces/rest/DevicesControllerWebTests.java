package com.kiniot.uflex.api.device.interfaces.rest;

import com.kiniot.uflex.api.device.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.device.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.device.domain.model.aggregates.Device;
import com.kiniot.uflex.api.device.domain.model.commands.AssignDeviceToPatientCommand;
import com.kiniot.uflex.api.device.domain.model.commands.UpdateDeviceStatusCommand;
import com.kiniot.uflex.api.device.domain.model.commands.UpdateDeviceTelemetryCommand;
import com.kiniot.uflex.api.device.domain.model.queries.GetDeviceByIdQuery;
import com.kiniot.uflex.api.device.domain.model.queries.GetDeviceBySerialNumberQuery;
import com.kiniot.uflex.api.device.domain.model.valueobjects.AdvertisedName;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceId;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceModel;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceStatus;
import com.kiniot.uflex.api.device.domain.model.valueobjects.FirmwareVersion;
import com.kiniot.uflex.api.device.domain.model.valueobjects.MacAddress;
import com.kiniot.uflex.api.device.domain.model.valueobjects.SerialNumber;
import com.kiniot.uflex.api.device.domain.services.DeviceCommandService;
import com.kiniot.uflex.api.device.domain.services.DeviceQueryService;
import com.kiniot.uflex.api.device.interfaces.rest.controllers.DevicesController;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DevicesControllerWebTests {

    private DeviceCommandService deviceCommandService;
    private DeviceQueryService deviceQueryService;
    private ExternalIamService externalIamService;
    private ExternalOrganizationService externalOrganizationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        deviceCommandService = mock(DeviceCommandService.class);
        deviceQueryService = mock(DeviceQueryService.class);
        externalIamService = mock(ExternalIamService.class);
        externalOrganizationService = mock(ExternalOrganizationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new DevicesController(
                deviceCommandService,
                deviceQueryService,
                externalIamService,
                externalOrganizationService
        )).build();
    }

    @Test
    void getDeviceByIdReturnsResourceWithAdvertisedNameAndLastSeenAt() throws Exception {
        var device = assignedDevice();
        when(deviceQueryService.handle(any(GetDeviceByIdQuery.class))).thenReturn(Optional.of(device));
        when(externalOrganizationService.getPatientFullName(device.getCurrentPatientId().patientId().toString()))
                .thenReturn("Ignacio Mestanza");

        mockMvc.perform(get("/api/v1/devices/" + device.getId().id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(device.getId().id().toString()))
                .andExpect(jsonPath("$.serialNumber").value("UFLEX-DEV-0012"))
                .andExpect(jsonPath("$.advertisedName").value("UFLEX-DEV-0012"))
                .andExpect(jsonPath("$.lastSeenAt").isNotEmpty())
                .andExpect(jsonPath("$.currentPatientFullName").value("Ignacio Mestanza"));
    }

    @Test
    void getDeviceBySerialNumberResolvesDevice() throws Exception {
        var device = assignedDevice();
        when(deviceQueryService.handle(any(GetDeviceBySerialNumberQuery.class))).thenReturn(Optional.of(device));
        when(externalOrganizationService.getPatientFullName(device.getCurrentPatientId().patientId().toString()))
                .thenReturn("Ignacio Mestanza");

        mockMvc.perform(get("/api/v1/devices/by-serial-number/UFLEX-DEV-0012"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(device.getId().id().toString()))
                .andExpect(jsonPath("$.serialNumber").value("UFLEX-DEV-0012"));
    }

    @Test
    void updateTelemetryUsesDeviceIdInCommand() throws Exception {
        var device = assignedDevice();
        doNothing().when(deviceCommandService).handle(any(UpdateDeviceTelemetryCommand.class));

        mockMvc.perform(patch("/api/v1/devices/" + device.getId().id() + "/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"batteryLevel":67}
                                """))
                .andExpect(status().isOk());

        var captor = ArgumentCaptor.forClass(UpdateDeviceTelemetryCommand.class);
        verify(deviceCommandService).handle(captor.capture());
        assertEquals(device.getId(), captor.getValue().deviceId());
        assertEquals(67, captor.getValue().batteryLevel());
    }

    @Test
    void updateStatusUsesDeviceIdInCommand() throws Exception {
        var device = assignedDevice();
        when(deviceQueryService.handle(any(GetDeviceByIdQuery.class))).thenReturn(Optional.of(device));
        when(externalOrganizationService.getPatientFullName(device.getCurrentPatientId().patientId().toString()))
                .thenReturn("Ignacio Mestanza");
        doNothing().when(deviceCommandService).handle(any(UpdateDeviceStatusCommand.class));

        mockMvc.perform(patch("/api/v1/devices/" + device.getId().id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ASSIGNED"}
                                """))
                .andExpect(status().isOk());

        var captor = ArgumentCaptor.forClass(UpdateDeviceStatusCommand.class);
        verify(deviceCommandService).handle(captor.capture());
        assertEquals(device.getId(), captor.getValue().deviceId());
        assertEquals(DeviceStatus.ASSIGNED, captor.getValue().status());
    }

    @Test
    void assignPatientUsesDeviceIdInCommand() throws Exception {
        var device = assignedDevice();
        when(deviceQueryService.handle(any(GetDeviceByIdQuery.class))).thenReturn(Optional.of(device));
        when(externalOrganizationService.getPatientFullName(device.getCurrentPatientId().patientId().toString()))
                .thenReturn("Ignacio Mestanza");
        doNothing().when(deviceCommandService).handle(any(AssignDeviceToPatientCommand.class));

        mockMvc.perform(post("/api/v1/devices/" + device.getId().id() + "/patient-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"patientId":"%s"}
                                """.formatted(device.getCurrentPatientId().patientId())))
                .andExpect(status().isOk());

        var captor = ArgumentCaptor.forClass(AssignDeviceToPatientCommand.class);
        verify(deviceCommandService).handle(captor.capture());
        assertEquals(device.getId(), captor.getValue().deviceId());
        assertEquals(device.getCurrentPatientId(), captor.getValue().patientId());
    }

    private Device assignedDevice() {
        var clinicId = new ClinicId(UUID.randomUUID());
        var device = new Device(
                new SerialNumber("UFLEX-DEV-0012"),
                new MacAddress("AA:BB:CC:DD:EE:FD"),
                new FirmwareVersion("1.0.0"),
                new DeviceModel("UFlex Tracker Pro"),
                new AdvertisedName("UFLEX-DEV-0012"),
                clinicId
        );
        device.assignToPatient(new PatientId(UUID.randomUUID()));
        device.recordTelemetry(84);
        return device;
    }
}
