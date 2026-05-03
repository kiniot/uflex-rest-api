package com.kiniot.uflex.api.iam.application.internal.eventhandlers;

import com.kiniot.uflex.api.iam.domain.exceptions.TenantAssignmentException;
import com.kiniot.uflex.api.iam.domain.model.commands.AssignUserTenantId;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.TenantId;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.iam.domain.services.UserCommandService;
import com.kiniot.uflex.api.organization.domain.model.events.ClinicAdminRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ClinicAdminRegisteredEventHandler {

    private final UserCommandService userCommandService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClinicAdminRegisteredEventHandler.class);

    public ClinicAdminRegisteredEventHandler(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @EventListener
    public void on(ClinicAdminRegisteredEvent event) {
        var userId = new UserId(UUID.fromString(event.getUserId()));
        var tenantId = new TenantId(UUID.fromString(event.getClinicId()));
        LOGGER.info("Handling ClinicAdminRegisteredEvent for User ID: {}, Clinic ID: {}", event.getUserId(), event.getClinicId());
        var assignUserTenantIdCommand = new AssignUserTenantId(userId, tenantId);
        try {
            userCommandService.handle(assignUserTenantIdCommand);
            LOGGER.info("Successfully assigned Tenant ID: {} to User ID: {}", event.getClinicId(), event.getUserId());
        } catch (Exception e) {
            LOGGER.error("Error assigning tenant ID to user: {}", e.getMessage());
            throw new IllegalArgumentException("Error assigning tenant ID to user: %s".formatted(e.getMessage()));
        }
    }
}
