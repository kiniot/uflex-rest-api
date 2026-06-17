package com.kiniot.uflex.api.device.interfaces.rest.controllers;

import com.kiniot.uflex.api.device.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.device.application.internal.outboundservices.acl.ExternalOrganizationService;
import com.kiniot.uflex.api.device.domain.model.commands.*;
import com.kiniot.uflex.api.device.domain.model.queries.*;
import com.kiniot.uflex.api.device.domain.model.valueobjects.DeviceId;
import com.kiniot.uflex.api.device.domain.model.valueobjects.SerialNumber;
import com.kiniot.uflex.api.device.domain.services.DeviceCommandService;
import com.kiniot.uflex.api.device.domain.services.DeviceQueryService;
import com.kiniot.uflex.api.device.interfaces.rest.resources.*;
import com.kiniot.uflex.api.device.interfaces.rest.transform.DeviceResourceFromEntityAssembler;
import com.kiniot.uflex.api.device.interfaces.rest.transform.RegisterDeviceCommandFromResourceAssembler;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/devices", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Devices", description = "Device Management Endpoints")
public class DevicesController {

    private final DeviceCommandService deviceCommandService;
    private final DeviceQueryService deviceQueryService;
    private final ExternalIamService externalIamService;
    private final ExternalOrganizationService externalOrganizationService;

    public DevicesController(
            DeviceCommandService deviceCommandService,
            DeviceQueryService deviceQueryService,
            ExternalIamService externalIamService,
            ExternalOrganizationService externalOrganizationService
    ) {
        this.deviceCommandService = deviceCommandService;
        this.deviceQueryService = deviceQueryService;
        this.externalIamService = externalIamService;
        this.externalOrganizationService = externalOrganizationService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CLINIC_ADMIN')")
    @Operation(summary = "Register a new device", description = "Creates a new device in the authenticated clinic administrator's clinic.")
    public ResponseEntity<DeviceResource> registerDevice(@RequestBody RegisterDeviceResource resource) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        var command = RegisterDeviceCommandFromResourceAssembler.toCommandFromResource(resource);
        var device = deviceCommandService.handle(command, clinicId);
        return new ResponseEntity<>(DeviceResourceFromEntityAssembler.toResourceFromEntity(device, null), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST')")
    @Operation(summary = "List all devices in clinic", description = "Returns all devices belonging to the authenticated clinic.")
    public ResponseEntity<List<DeviceResource>> getAllDevices() {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        var query = new GetAllDevicesByClinicIdQuery(clinicId, null);
        var devices = deviceQueryService.handle(query);
        var resources = devices.stream()
                .map(d -> {
                    String patientName = d.getCurrentPatientId() != null
                            ? externalOrganizationService.getPatientFullName(d.getCurrentPatientId().patientId().toString())
                            : null;
                    return DeviceResourceFromEntityAssembler.toResourceFromEntity(d, patientName);
                })
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{deviceId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST')")
    @Operation(summary = "Get device by id", description = "Returns a device by its internal identifier within the authenticated clinic.")
    public ResponseEntity<DeviceResource> getDeviceById(@PathVariable String deviceId) {
        return deviceQueryService.handle(new GetDeviceByIdQuery(toDeviceId(deviceId)))
                .map(this::toDeviceResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-serial-number/{serialNumber}")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST')")
    @Operation(summary = "Get device by serial number", description = "Resolves a device by its physical serial number within the authenticated clinic.")
    public ResponseEntity<DeviceResource> getDeviceBySerialNumber(@PathVariable String serialNumber) {
        return deviceQueryService.handle(new GetDeviceBySerialNumberQuery(new SerialNumber(serialNumber)))
                .map(this::toDeviceResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST')")
    @Operation(summary = "Get clinic fleet metrics", description = "Returns device fleet metrics for the authenticated clinic.")
    public ResponseEntity<ClinicFleetMetricsResource> getClinicFleetMetrics() {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        var query = new GetClinicFleetMetricsQuery(clinicId);
        var metrics = deviceQueryService.handle(query);
        return ResponseEntity.ok(new ClinicFleetMetricsResource(
                metrics.total(),
                metrics.available(),
                metrics.assigned(),
                metrics.inMaintenance(),
                metrics.lowBattery(),
                metrics.offline()
        ));
    }

    @PatchMapping("/{deviceId}/status")
    @PreAuthorize("hasAuthority('ROLE_CLINIC_ADMIN')")
    @Operation(summary = "Update device status", description = "Updates the status of a device.")
    public ResponseEntity<DeviceResource> updateDeviceStatus(
            @PathVariable String deviceId,
            @RequestBody UpdateDeviceStatusResource resource
    ) {
        var resolvedDeviceId = toDeviceId(deviceId);
        var command = new UpdateDeviceStatusCommand(resolvedDeviceId, resource.status());
        deviceCommandService.handle(command);
        return deviceQueryService.handle(new GetDeviceByIdQuery(resolvedDeviceId))
                .map(this::toDeviceResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{deviceId}/calibration")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST')")
    @Operation(summary = "Manage device calibration",
            description = "Either marks calibration as invalid (needs_calibration) or registers a successful calibration (validate).")
    public ResponseEntity<DeviceResource> updateCalibration(
            @PathVariable String deviceId,
            @RequestBody CalibrationActionResource resource
    ) {
        var resolvedDeviceId = toDeviceId(deviceId);
        if ("validate".equalsIgnoreCase(resource.action())) {
            var command = new RegisterSuccessfulCalibrationCommand(resolvedDeviceId);
            deviceCommandService.handle(command);
        } else if ("needs_calibration".equalsIgnoreCase(resource.action())) {
            var command = new MarkCalibrationAsInvalidCommand(resolvedDeviceId);
            deviceCommandService.handle(command);
        }
        return deviceQueryService.handle(new GetDeviceByIdQuery(resolvedDeviceId))
                .map(this::toDeviceResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{deviceId}/patient-assignments")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST')")
    @Operation(summary = "Assign device to a patient", description = "Assigns a device to a patient within the authenticated clinic.")
    public ResponseEntity<DeviceResource> assignDeviceToPatient(
            @PathVariable String deviceId,
            @RequestBody AssignDeviceToPatientResource resource
    ) {
        var patientId = new PatientId(UUID.fromString(resource.patientId()));
        var resolvedDeviceId = toDeviceId(deviceId);
        var command = new AssignDeviceToPatientCommand(resolvedDeviceId, patientId);
        deviceCommandService.handle(command);
        return deviceQueryService.handle(new GetDeviceByIdQuery(resolvedDeviceId))
                .map(this::toDeviceResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{deviceId}/patient-assignments")
    @PreAuthorize("hasAuthority('ROLE_PHYSIOTHERAPIST')")
    @Operation(summary = "Return device from patient", description = "Returns a device from its current patient assignment.")
    public ResponseEntity<Void> returnDevice(@PathVariable String deviceId) {
        var command = new ReturnDeviceCommand(toDeviceId(deviceId));
        deviceCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-assigned")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @Operation(summary = "Get my assigned device", description = "Returns the device assigned to the authenticated patient.")
    public ResponseEntity<DeviceResource> getMyAssignedDevice() {
        var patientIdOpt = externalIamService.getCurrentPatientId();
        if (patientIdOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var device = deviceQueryService.handle(new GetMyAssignedDeviceQuery(patientIdOpt.get()));
        return device.map(d -> {
            String patientName = d.getCurrentPatientId() != null
                    ? externalOrganizationService.getPatientFullName(d.getCurrentPatientId().patientId().toString())
                    : null;
            return ResponseEntity.ok(DeviceResourceFromEntityAssembler.toResourceFromEntity(d, patientName));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{deviceId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST')")
    @Operation(summary = "Delete a device", description = "Deletes a device by internal identifier.")
    public ResponseEntity<Void> deleteDevice(@PathVariable String deviceId) {
        var command = new DeleteDeviceCommand(toDeviceId(deviceId));
        deviceCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{deviceId}/telemetry")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_PATIENT', 'ROLE_PHYSIOTHERAPIST')")
    @Operation(summary = "Update device telemetry", description = "Updates the battery level of a device.")
    public ResponseEntity<Void> updateTelemetry(
            @PathVariable String deviceId,
            @RequestBody UpdateTelemetryResource resource
    ) {
        var command = new UpdateDeviceTelemetryCommand(toDeviceId(deviceId), resource.batteryLevel());
        deviceCommandService.handle(command);
        return ResponseEntity.ok().build();
    }

    private DeviceResource toDeviceResource(com.kiniot.uflex.api.device.domain.model.aggregates.Device device) {
        String patientName = device.getCurrentPatientId() != null
                ? externalOrganizationService.getPatientFullName(device.getCurrentPatientId().patientId().toString())
                : null;
        return DeviceResourceFromEntityAssembler.toResourceFromEntity(device, patientName);
    }

    private DeviceId toDeviceId(String deviceId) {
        return new DeviceId(UUID.fromString(deviceId));
    }
}
