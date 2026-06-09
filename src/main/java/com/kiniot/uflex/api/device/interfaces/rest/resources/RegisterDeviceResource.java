package com.kiniot.uflex.api.device.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterDeviceResource(
        @Schema(description = "Device serial number", example = "UFLEX-DEV-001")
        String serialNumber,
        @Schema(description = "Device MAC address", example = "AA:BB:CC:DD:EE:FF")
        String macAddress,
        @Schema(description = "Device firmware version", example = "1.0.0")
        String firmwareVersion,
        @Schema(description = "Device model name", example = "UFlex Tracker Pro")
        String model
) {}