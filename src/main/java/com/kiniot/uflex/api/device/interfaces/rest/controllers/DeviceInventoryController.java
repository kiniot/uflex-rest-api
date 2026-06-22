package com.kiniot.uflex.api.device.interfaces.rest.controllers;

import com.kiniot.uflex.api.device.domain.model.commands.FulfillClinicCommand;
import com.kiniot.uflex.api.device.domain.model.queries.GetFleetHealthQuery;
import com.kiniot.uflex.api.device.domain.model.queries.GetFulfillmentQueueQuery;
import com.kiniot.uflex.api.device.domain.model.queries.GetGlobalDeviceOverviewQuery;
import com.kiniot.uflex.api.device.domain.model.queries.GetStockDevicesQuery;
import com.kiniot.uflex.api.device.domain.services.DeviceCommandService;
import com.kiniot.uflex.api.device.domain.services.DeviceQueryService;
import com.kiniot.uflex.api.device.interfaces.rest.resources.DeviceResource;
import com.kiniot.uflex.api.device.interfaces.rest.resources.FleetHealthResource;
import com.kiniot.uflex.api.device.interfaces.rest.resources.FulfillResultResource;
import com.kiniot.uflex.api.device.interfaces.rest.resources.FulfillmentRowResource;
import com.kiniot.uflex.api.device.interfaces.rest.resources.GlobalDeviceOverviewResource;
import com.kiniot.uflex.api.device.interfaces.rest.resources.RegisterDeviceResource;
import com.kiniot.uflex.api.device.interfaces.rest.resources.RegisterStockDevicesBatchResource;
import com.kiniot.uflex.api.device.interfaces.rest.transform.DeviceResourceFromEntityAssembler;
import com.kiniot.uflex.api.device.interfaces.rest.transform.FleetHealthResourceFromResultAssembler;
import com.kiniot.uflex.api.device.interfaces.rest.transform.FulfillmentRowResourceFromResultAssembler;
import com.kiniot.uflex.api.device.interfaces.rest.transform.GlobalDeviceOverviewResourceFromResultAssembler;
import com.kiniot.uflex.api.device.interfaces.rest.transform.RegisterDeviceCommandFromResourceAssembler;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Global device inventory (stock) management — a platform/operations concern restricted to
 * the technical team ({@code ROLE_DEVELOPER}). Devices registered here are clinic-less and
 * stay {@code IN_STOCK} until a clinic's subscription activation assigns them automatically.
 */
@RestController
@RequestMapping(value = "/api/v1/devices", produces = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping("/stock")
    @Operation(summary = "Register a device into stock",
            description = "Adds a single device to the global, clinic-less inventory.")
    public ResponseEntity<DeviceResource> registerStockDevice(@RequestBody RegisterDeviceResource resource) {
        var command = RegisterDeviceCommandFromResourceAssembler.toCommandFromResource(resource);
        var device = deviceCommandService.handle(command);
        return new ResponseEntity<>(DeviceResourceFromEntityAssembler.toResourceFromEntity(device, null), HttpStatus.CREATED);
    }

    @PostMapping("/stock/batch")
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

    @GetMapping("/stock")
    @Operation(summary = "List devices in stock",
            description = "Returns all devices currently in the global, clinic-less inventory.")
    public ResponseEntity<List<DeviceResource>> getStockDevices() {
        var resources = deviceQueryService.handle(new GetStockDevicesQuery()).stream()
                .map(device -> DeviceResourceFromEntityAssembler.toResourceFromEntity(device, null))
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/overview")
    @Operation(summary = "Global device inventory overview",
            description = "Cross-clinic aggregate counts plus per-clinic device totals.")
    public ResponseEntity<GlobalDeviceOverviewResource> getOverview() {
        var overview = deviceQueryService.handle(new GetGlobalDeviceOverviewQuery());
        return ResponseEntity.ok(GlobalDeviceOverviewResourceFromResultAssembler.toResourceFromResult(overview));
    }

    @GetMapping("/fulfillment")
    @Operation(summary = "Fulfillment queue",
            description = "Clinics that paid for more kits than they own (pending shipment), ordered by shortfall.")
    public ResponseEntity<List<FulfillmentRowResource>> getFulfillmentQueue() {
        var rows = deviceQueryService.handle(new GetFulfillmentQueueQuery()).stream()
                .map(FulfillmentRowResourceFromResultAssembler::toResourceFromResult)
                .toList();
        return ResponseEntity.ok(rows);
    }

    @PostMapping("/fulfillment/{clinicId}")
    @Operation(summary = "Fulfill a clinic from stock",
            description = "Assigns available stock to the clinic to cover its kit shortfall. Returns how many were assigned.")
    public ResponseEntity<FulfillResultResource> fulfillClinic(@PathVariable String clinicId) {
        var clinic = new ClinicId(UUID.fromString(clinicId));
        int assigned = deviceCommandService.handle(new FulfillClinicCommand(clinic));
        return ResponseEntity.ok(new FulfillResultResource(clinicId, assigned));
    }

    @GetMapping("/health")
    @Operation(summary = "Fleet health",
            description = "Cross-clinic deployed devices that are offline, low on battery, or need calibration.")
    public ResponseEntity<FleetHealthResource> getFleetHealth() {
        var health = deviceQueryService.handle(new GetFleetHealthQuery());
        return ResponseEntity.ok(FleetHealthResourceFromResultAssembler.toResourceFromResult(health));
    }
}
