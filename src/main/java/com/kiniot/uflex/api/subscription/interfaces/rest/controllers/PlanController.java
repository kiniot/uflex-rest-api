package com.kiniot.uflex.api.subscription.interfaces.rest.controllers;

import com.kiniot.uflex.api.subscription.domain.model.queries.GetPlanByIdQuery;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetPlanListQuery;
import com.kiniot.uflex.api.subscription.domain.services.PlanQueryService;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.PlanResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.PlanResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/plans", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subscription Plans", description = "Available subscription plan endpoints")
public class PlanController {
    private final PlanQueryService planQueryService;

    public PlanController(PlanQueryService planQueryService) {
        this.planQueryService = planQueryService;
    }

    @GetMapping
    public ResponseEntity<List<PlanResource>> getPlans() {
        var plans = planQueryService.handle(new GetPlanListQuery()).stream()
                .map(PlanResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanResource> getPlanById(@PathVariable UUID id) {
        return planQueryService.handle(new GetPlanByIdQuery(id))
                .map(PlanResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
