package com.kiniot.uflex.api.device.interfaces.rest.controllers;

import com.kiniot.uflex.api.device.domain.model.queries.GetStockDevicesQuery;
import com.kiniot.uflex.api.device.domain.services.DeviceCommandService;
import com.kiniot.uflex.api.device.domain.services.DeviceQueryService;
import com.kiniot.uflex.api.device.interfaces.rest.resources.DeviceResource;
import com.kiniot.uflex.api.device.interfaces.rest.resources.RegisterDeviceResource;
import com.kiniot.uflex.api.device.interfaces.rest.resources.RegisterStockDevicesBatchResource;
import com.kiniot.uflex.api.device.interfaces.rest.transform.DeviceResourceFromEntityAssembler;
import com.kiniot.uflex.api.device.interfaces.rest.transform.RegisterDeviceCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Global device inventory (stock) management — a platform/operations concern restricted to
 * the technical team ({@code ROLE_DEVELOPER}). Devices registered here are clinic-less and
 * stay {@code IN_STOCK} until a clinic's subscription activation assigns them automatically.
 */
@RestController
@RequestMapping(value = "/api/v1/devices/stock", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Device Inventory", description = "Global device inventory (stock) endpoints — ROLE_DEVELOPER only")
@PreAuthorize("hasAuthority('ROLE_DEVELOPER')")
public class DeviceInventoryController {

    private final DeviceCommandService deviceCommandService;
    private final DeviceQueryService deviceQueryService;

    public DeviceInventoryController(
            DeviceCommandService deviceCommandService,
            DeviceQueryService deviceQueryService
    ) {
        this.deviceCommandService = deviceCommandService;
        this.deviceQueryService = deviceQueryService;
    }

    @PostMapping
    @Operation(summary = "Register a device into stock",
            description = "Adds a single device to the global, clinic-less inventory.")
    public ResponseEntity<DeviceResource> registerStockDevice(@RequestBody RegisterDeviceResource resource) {
        var command = RegisterDeviceCommandFromResourceAssembler.toCommandFromResource(resource);
        var device = deviceCommandService.handle(command);
        return new ResponseEntity<>(DeviceResourceFromEntityAssembler.toResourceFromEntity(device, null), HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    @Operation(summary = "Register a batch of devices into stock",
            description = "Adds multiple devices to the global inventory in a single request.")
    public ResponseEntity<List<DeviceResource>> registerStockDevicesBatch(@RequestBody RegisterStockDevicesBatchResource resource) {
        var created = resource.devices().stream()
                .map(RegisterDeviceCommandFromResourceAssembler::toCommandFromResource)
                .map(deviceCommandService::handle)
                .map(device -> DeviceResourceFromEntityAssembler.toResourceFromEntity(device, null))
                .toList();
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List devices in stock",
            description = "Returns all devices currently in the global, clinic-less inventory.")
    public ResponseEntity<List<DeviceResource>> getStockDevices() {
        var resources = deviceQueryService.handle(new GetStockDevicesQuery()).stream()
                .map(device -> DeviceResourceFromEntityAssembler.toResourceFromEntity(device, null))
                .toList();
        return ResponseEntity.ok(resources);
    }
}
