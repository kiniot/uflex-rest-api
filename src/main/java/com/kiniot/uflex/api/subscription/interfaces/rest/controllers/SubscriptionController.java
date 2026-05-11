package com.kiniot.uflex.api.subscription.interfaces.rest.controllers;

import com.kiniot.uflex.api.subscription.domain.model.queries.GetInvoiceHistoryQuery;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetSubscriptionByClinicQuery;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionCommandService;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionQueryService;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.CancelSubscriptionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.ChangePlanResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.InvoiceResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.PurchaseSubscriptionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.SubscriptionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.UpdatePaymentMethodResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.CancelSubscriptionCommandFromResourceAssembler;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.ChangeSubscriptionPlanCommandFromResourceAssembler;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.InvoiceResourceFromEntityAssembler;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.PurchaseSubscriptionCommandFromResourceAssembler;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.SubscriptionResourceFromEntityAssembler;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.UpdatePaymentMethodCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subscriptions", description = "Available subscription endpoints")
public class SubscriptionController {
    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionQueryService subscriptionQueryService;

    public SubscriptionController(SubscriptionCommandService subscriptionCommandService,
                                  SubscriptionQueryService subscriptionQueryService) {
        this.subscriptionCommandService = subscriptionCommandService;
        this.subscriptionQueryService = subscriptionQueryService;
    }

    @GetMapping
    public ResponseEntity<SubscriptionResource> getSubscriptionByClinic(@RequestParam UUID clinicId) {
        return subscriptionQueryService.handle(new GetSubscriptionByClinicQuery(clinicId))
                .map(SubscriptionResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SubscriptionResource> purchaseSubscription(@RequestBody PurchaseSubscriptionResource resource) {
        try {
            var command = PurchaseSubscriptionCommandFromResourceAssembler.toCommandFromResource(resource);
            return subscriptionCommandService.handle(command)
                    .map(SubscriptionResourceFromEntityAssembler::toResourceFromEntity)
                    .map(subscription -> new ResponseEntity<>(subscription, HttpStatus.CREATED))
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @PatchMapping("/{id}/plan")
    public ResponseEntity<SubscriptionResource> changePlan(@PathVariable UUID id, @RequestBody ChangePlanResource resource) {
        try {
            var command = ChangeSubscriptionPlanCommandFromResourceAssembler.toCommandFromResource(id, resource);
            return subscriptionCommandService.handle(command)
                    .map(SubscriptionResourceFromEntityAssembler::toResourceFromEntity)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @PatchMapping("/{id}/payment-method")
    public ResponseEntity<SubscriptionResource> updatePaymentMethod(@PathVariable UUID id,
                                                                    @RequestBody UpdatePaymentMethodResource resource) {
        try {
            var command = UpdatePaymentMethodCommandFromResourceAssembler.toCommandFromResource(id, resource);
            return subscriptionCommandService.handle(command)
                    .map(SubscriptionResourceFromEntityAssembler::toResourceFromEntity)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<SubscriptionResource> cancelSubscription(@PathVariable UUID id,
                                                                  @RequestBody(required = false) CancelSubscriptionResource resource) {
        try {
            var command = CancelSubscriptionCommandFromResourceAssembler.toCommandFromResource(id, resource);
            return subscriptionCommandService.handle(command)
                    .map(SubscriptionResourceFromEntityAssembler::toResourceFromEntity)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @GetMapping("/{id}/invoices")
    public ResponseEntity<List<InvoiceResource>> getInvoiceHistory(@PathVariable UUID id) {
        var invoices = subscriptionQueryService.handle(new GetInvoiceHistoryQuery(id)).stream()
                .map(InvoiceResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(invoices);
    }
}
