package com.kiniot.uflex.api.device.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record RegisterStockDevicesBatchResource(
        @Schema(description = "Devices to register into the global inventory (stock)")
        List<RegisterDeviceResource> devices
) {}
