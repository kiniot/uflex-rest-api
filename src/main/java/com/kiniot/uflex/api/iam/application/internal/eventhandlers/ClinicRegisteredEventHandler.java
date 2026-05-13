package com.kiniot.uflex.api.iam.application.internal.eventhandlers;

import com.kiniot.uflex.api.iam.domain.model.commands.AssignUserRoleCommand;
import com.kiniot.uflex.api.iam.domain.model.commands.AssignUserTenantId;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.RoleName;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.TenantId;
import com.kiniot.uflex.api.iam.domain.services.UserCommandService;
import com.kiniot.uflex.api.organization.domain.model.events.ClinicRegisteredEvent;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ClinicRegisteredEventHandler {

    private final UserCommandService userCommandService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClinicRegisteredEventHandler.class);

    public ClinicRegisteredEventHandler(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @EventListener
    public void on(ClinicRegisteredEvent event) {
        var userId = new UserId(UUID.fromString(event.getUserId()));
        var tenantId = new TenantId(UUID.fromString(event.getClinicId()));
        LOGGER.info("Handling ClinicRegisteredEvent for User ID: {}, Clinic ID: {}", event.getUserId(), event.getClinicId());
        var assignUserTenantIdCommand = new AssignUserTenantId(userId, tenantId);
        var assignUserRoleCommand = new AssignUserRoleCommand(userId, RoleName.ROLE_CLINIC_ADMIN);
        try {
            userCommandService.handle(assignUserTenantIdCommand);
            userCommandService.handle(assignUserRoleCommand);
            LOGGER.info("Successfully assigned Tenant ID: {} to User ID: {}", event.getClinicId(), event.getUserId());
        } catch (Exception e) {
            LOGGER.error("Error assigning clinic ownership data to user: {}", e.getMessage());
            throw new IllegalArgumentException("Error assigning clinic ownership data to user: %s".formatted(e.getMessage()));
        }
    }
}
