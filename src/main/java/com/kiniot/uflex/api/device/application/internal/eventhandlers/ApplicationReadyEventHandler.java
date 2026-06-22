package com.kiniot.uflex.api.device.application.internal.eventhandlers;

import com.kiniot.uflex.api.device.domain.model.commands.SeedDevicesCommand;
import com.kiniot.uflex.api.device.domain.services.DeviceCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Seeds demo stock devices on startup for the {@code dev} profile only. Production stock is
 * managed through the ROLE_DEVELOPER inventory endpoints.
 */
@Service("deviceApplicationReadyEventHandler")
@Profile("dev")
public class ApplicationReadyEventHandler {

    private final DeviceCommandService deviceCommandService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationReadyEventHandler.class);

    public ApplicationReadyEventHandler(DeviceCommandService deviceCommandService) {
        this.deviceCommandService = deviceCommandService;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        int created = deviceCommandService.handle(new SeedDevicesCommand());
        LOGGER.info("Device stock seeding completed ({} new device(s) created)", created);
    }
}
