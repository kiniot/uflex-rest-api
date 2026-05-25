package com.kiniot.uflex.api.subscription.interfaces.rest.controllers;

import com.kiniot.uflex.api.subscription.domain.model.commands.GetCurrentSubscriptionQuery;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetAllTiersQuery;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionCommandService;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionQueryService;
import com.kiniot.uflex.api.subscription.domain.services.TierQueryService;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.CheckoutSessionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.CreateSubscriptionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.SubscriptionResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.TierCatalogResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.CheckoutSessionResourceFromResultAssembler;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.CreateSubscriptionCommandFromResourceAssembler;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.SubscriptionResourceFromEntityAssembler;
import com.kiniot.uflex.api.subscription.interfaces.rest.transform.TierCatalogResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subscriptions", description = "Available subscriptions endpoints")
public class SubscriptionsController {

    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionQueryService subscriptionQueryService;
    private final TierQueryService tierQueryService;

    public SubscriptionsController(
            SubscriptionCommandService subscriptionCommandService,
            SubscriptionQueryService subscriptionQueryService,
            TierQueryService tierQueryService
    ) {
        this.subscriptionCommandService = subscriptionCommandService;
        this.subscriptionQueryService = subscriptionQueryService;
        this.tierQueryService = tierQueryService;
    }

    @PostMapping("/checkout")
    @Operation(summary = "Create subscription checkout session",
            description = "Creates a pending subscription for the authenticated clinic and returns the Stripe checkout session URL.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Subscription selection and pricing data for checkout creation.",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = CreateSubscriptionResource.class),
                    examples = @ExampleObject(
                            name = "Create subscription checkout",
                            value = """
                                    {
                                      "tierId": "019e4c9b-a972-729b-8e35-667d1fb56d38",
                                      "billingPeriod": "MONTHLY",
                                      "amount": 90.00,
                                      "currency": "USD",
                                      "requestedTotalKits": 1
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Checkout session created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid subscription data or checkout could not be created"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or authenticated user has no clinic")
    })
    public ResponseEntity<CheckoutSessionResource> createSubscriptionCheckout(@RequestBody CreateSubscriptionResource resource) {
        var command = CreateSubscriptionCommandFromResourceAssembler.toCommandFromResource(resource);
        var subscription = subscriptionCommandService.handle(command);
        if (subscription.isEmpty()) return ResponseEntity.badRequest().build();
        var checkoutSessionResource = CheckoutSessionResourceFromResultAssembler.toResourceFromResult(subscription.get());
        return new ResponseEntity<>(checkoutSessionResource, HttpStatus.CREATED);
    }

    @GetMapping("/current")
    @Operation(summary = "Get current subscription",
            description = "Returns the current subscription for the authenticated clinic, if one exists.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current subscription retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or authenticated user has no clinic"),
            @ApiResponse(responseCode = "404", description = "No current subscription found for the authenticated clinic")
    })
    public ResponseEntity<SubscriptionResource> getCurrentSubscription() {
        return subscriptionQueryService.handle(new GetCurrentSubscriptionQuery())
                .map(SubscriptionResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/tiers")
    @Operation(summary = "Get subscription tiers catalog",
            description = "Returns the subscription tiers currently available to be displayed by clients.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription tiers retrieved successfully")
    })
    public ResponseEntity<List<TierCatalogResource>> getSubscriptionTiers() {
        var resources = tierQueryService.handle(new GetAllTiersQuery()).stream()
                .map(TierCatalogResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}
