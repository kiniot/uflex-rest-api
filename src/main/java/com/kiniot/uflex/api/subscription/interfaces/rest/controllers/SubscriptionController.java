package com.kiniot.uflex.api.subscription.interfaces.rest.controllers;

import com.kiniot.uflex.api.subscription.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.PaymentGatewayPort;
import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetInvoiceHistoryQuery;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetSubscriptionByClinicQuery;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionCommandService;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionQueryService;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.CancelSubscriptionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.ChangePlanResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.CurrentSubscriptionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.InvoiceResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.PurchaseSubscriptionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.StripeInvoiceResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.StripePaymentMethodResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.SubscriptionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.UpdatePaymentMethodResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.CancelSubscriptionCommandFromResourceAssembler;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.ChangeSubscriptionPlanCommandFromResourceAssembler;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.CurrentSubscriptionResourceFromEntityAssembler;
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
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subscriptions", description = "Available subscription endpoints")
public class SubscriptionController {
    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionQueryService subscriptionQueryService;
    private final ExternalIamService externalIamService;
    private final PaymentGatewayPort paymentGatewayPort;

    public SubscriptionController(SubscriptionCommandService subscriptionCommandService,
                                  SubscriptionQueryService subscriptionQueryService,
                                  ExternalIamService externalIamService,
                                  PaymentGatewayPort paymentGatewayPort) {
        this.subscriptionCommandService = subscriptionCommandService;
        this.subscriptionQueryService = subscriptionQueryService;
        this.externalIamService = externalIamService;
        this.paymentGatewayPort = paymentGatewayPort;
    }

    @GetMapping("/current")
    public ResponseEntity<CurrentSubscriptionResource> getCurrentSubscription() {
        return currentSubscription()
                .map(CurrentSubscriptionResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok().body(null));
    }

    @GetMapping("/payment-method")
    public ResponseEntity<StripePaymentMethodResource> getPaymentMethod() {
        try {
            var paymentMethod = currentSubscription()
                    .map(Subscription::getPaymentReference)
                    .flatMap(paymentGatewayPort::getPaymentMethod)
                    .map(method -> new StripePaymentMethodResource(method.brand(), method.last4(), method.expMonth(), method.expYear()))
                    .orElse(null);
            return ResponseEntity.ok(paymentMethod);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, exception.getMessage(), exception);
        }
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<StripeInvoiceResource>> getCurrentSubscriptionInvoices() {
        try {
            var invoices = currentSubscription()
                    .map(Subscription::getPaymentReference)
                    .map(paymentGatewayPort::getInvoices)
                    .orElseGet(List::of)
                    .stream()
                    .map(invoice -> new StripeInvoiceResource(
                            invoice.invoiceId(),
                            invoice.number(),
                            invoice.issuedDate(),
                            invoice.dueDate(),
                            invoice.amount(),
                            invoice.currency(),
                            invoice.status(),
                            invoice.hostedInvoiceUrl()
                    ))
                    .toList();
            return ResponseEntity.ok(invoices);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, exception.getMessage(), exception);
        }
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

    private Optional<Subscription> currentSubscription() {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authenticated user has no clinic assigned"));
        return subscriptionQueryService.handle(new GetSubscriptionByClinicQuery(clinicId.clinicId()));
    }
}
