package com.kiniot.uflex.api.subscription.application.internal.eventhandlers;

import com.kiniot.uflex.api.subscription.domain.model.commands.SeedTiersCommand;
import com.kiniot.uflex.api.subscription.domain.services.TierCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service("subscriptionApplicationReadyEventHandler")
public class ApplicationReadyEventHandler {

    private final TierCommandService tierCommandService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationReadyEventHandler.class);

    public ApplicationReadyEventHandler(TierCommandService tierCommandService) {
        this.tierCommandService = tierCommandService;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        var applicationName = event.getApplicationContext().getId();
        LOGGER.info("Starting to verify if tiers seeding is needed for {} at {}", applicationName, currentTimestamp());
        var seedTiersCommand = new SeedTiersCommand();
        tierCommandService.handle(seedTiersCommand);
        LOGGER.info("Tiers seeding verification completed for {} at {}", applicationName, currentTimestamp());
    }

    private Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}
