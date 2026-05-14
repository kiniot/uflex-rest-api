package com.kiniot.uflex.api.subscription.application.internal.eventhandlers;

import com.kiniot.uflex.api.subscription.domain.model.commands.CreatePlanCommand;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.services.PlanCommandService;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.PlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class SubscriptionPlanSeederApplicationReadyEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPlanSeederApplicationReadyEventHandler.class);

    private final PlanRepository planRepository;
    private final PlanCommandService planCommandService;

    public SubscriptionPlanSeederApplicationReadyEventHandler(PlanRepository planRepository, PlanCommandService planCommandService) {
        this.planRepository = planRepository;
        this.planCommandService = planCommandService;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        var applicationName = event.getApplicationContext().getId();
        LOGGER.info("Starting to verify if subscription plans seeding is needed for {} at {}", applicationName, currentTimestamp());
        seedPlan(new CreatePlanCommand(
                "Starter",
                "STARTER",
                new Money(java.math.BigDecimal.valueOf(149), "PEN"),
                new Money(java.math.BigDecimal.valueOf(1490), "PEN"),
                30,
                2,
                List.of("Seguimiento basico", "Dashboard clinico", "Historial de sesiones")
        ));
        seedPlan(new CreatePlanCommand(
                "Professional",
                "PROFESSIONAL",
                new Money(java.math.BigDecimal.valueOf(299), "PEN"),
                new Money(java.math.BigDecimal.valueOf(2990), "PEN"),
                120,
                8,
                List.of("Monitoreo ROM avanzado", "Reportes clinicos", "Alertas", "Soporte prioritario")
        ));
        seedPlan(new CreatePlanCommand(
                "Enterprise",
                "ENTERPRISE",
                new Money(java.math.BigDecimal.valueOf(599), "PEN"),
                new Money(java.math.BigDecimal.valueOf(5990), "PEN"),
                500,
                30,
                List.of("Multi-sede", "Integracion avanzada", "Auditoria", "SLA empresarial")
        ));
        LOGGER.info("Subscription plans seeding verification completed for {} at {}", applicationName, currentTimestamp());
    }

    private void seedPlan(CreatePlanCommand command) {
        if (!planRepository.existsByCode(command.code())) {
            planCommandService.handle(command);
        }
    }

    private Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}
