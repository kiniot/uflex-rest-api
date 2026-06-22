package com.kiniot.uflex.api.device.application.internal.eventhandlers;

import com.kiniot.uflex.api.device.domain.model.commands.AssignStockToClinicCommand;
import com.kiniot.uflex.api.device.domain.services.DeviceCommandService;
import com.kiniot.uflex.api.subscription.domain.model.events.SubscriptionActivatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Assigns stock devices to a clinic when its subscription becomes ACTIVE. Assignment is
 * idempotent: only the shortfall between the kits paid for and the kits already owned is
 * covered, capped by available stock. A shortage of stock is logged but never fails the
 * activation — the remaining kits stay pending (derived) until stock is replenished.
 */
@Service
public class SubscriptionActivatedEventHandler {

    private final DeviceCommandService deviceCommandService;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionActivatedEventHandler.class);

    public SubscriptionActivatedEventHandler(DeviceCommandService deviceCommandService) {
        this.deviceCommandService = deviceCommandService;
    }

    @EventListener
    public void on(SubscriptionActivatedEvent event) {
        var clinicId = event.getClinicId();
        var requestedTotalKits = event.getRequestedTotalKits() == null ? 0 : event.getRequestedTotalKits();
        LOGGER.info("Handling SubscriptionActivatedEvent for Clinic ID: {} requesting {} kit(s)",
                clinicId.id(), requestedTotalKits);
        try {
            int assigned = deviceCommandService.handle(new AssignStockToClinicCommand(clinicId, requestedTotalKits));
            LOGGER.info("Assigned {} stock device(s) to Clinic ID: {} for {} requested kit(s)",
                    assigned, clinicId.id(), requestedTotalKits);
            if (assigned < requestedTotalKits) {
                // The authoritative pending count is derived in the fleet metrics
                // (requested - kits the clinic already owns); it is not recomputed here.
                LOGGER.warn("Stock was insufficient to fully cover the {} requested kit(s) for Clinic ID: {}; "
                        + "remaining kits stay pending until stock is replenished", requestedTotalKits, clinicId.id());
            }
        } catch (Exception e) {
            LOGGER.error("Error assigning stock devices to Clinic ID: {}: {}", clinicId.id(), e.getMessage());
        }
    }
}
