package com.kiniot.uflex.api.device.domain.model.commands;

/**
 * Seeds a small set of demo stock devices for local/demo environments. Idempotent: devices
 * whose serial number already exists are skipped. Production stock is managed via the
 * ROLE_DEVELOPER inventory endpoints, not this seed.
 */
public record SeedDevicesCommand() {
}
