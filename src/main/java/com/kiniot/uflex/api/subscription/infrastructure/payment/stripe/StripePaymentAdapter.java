package com.kiniot.uflex.api.subscription.infrastructure.payment.stripe;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.CheckoutSessionResult;
import com.kiniot.uflex.api.subscription.domain.model.commands.CreateCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.PaymentGatewayPort;
import com.kiniot.uflex.api.subscription.infrastructure.payment.stripe.configuration.StripePaymentProperties;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class StripePaymentAdapter implements PaymentGatewayPort {

    private final StripePaymentProperties stripePaymentProperties;

    public StripePaymentAdapter(StripePaymentProperties stripePaymentProperties) {
        this.stripePaymentProperties = stripePaymentProperties;
    }

    @Override
    public CheckoutSessionResult createCheckoutSession(CreateCheckoutSessionCommand command) {
        if (!stripePaymentProperties.isEnabled()) {
            throw new IllegalStateException("Stripe payments are disabled");
        }
        if (stripePaymentProperties.getSecretKey() == null || stripePaymentProperties.getSecretKey().isBlank()) {
            throw new IllegalStateException("Stripe secret key is not configured");
        }

        Stripe.apiKey = stripePaymentProperties.getSecretKey();

        var planAmountInMinorUnits = toMinorUnits(command.recurringPlanAmount().amount());

        var paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripePaymentProperties.getSuccessUrl())
                .setCancelUrl(stripePaymentProperties.getCancelUrl())
                .putMetadata("subscription_id", command.subscriptionId().id().toString())
                .putMetadata("clinic_id", command.clinicId().id().toString())
                .putMetadata("tier_id", command.selection().tierId().id().toString())
                .putMetadata("tier_name", command.tierName())
                .putMetadata("billing_period", command.selection().billingPeriod().name());

        paramsBuilder.addLineItem(
                SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency(command.recurringPlanAmount().currency().name().toLowerCase())
                                        .setUnitAmount(planAmountInMinorUnits)
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName("uFlex Subscription Plan")
                                                        .setDescription("%s - %s".formatted(
                                                                command.tierName(),
                                                                command.selection().billingPeriod().name()
                                                        ))
                                                        .build()
                                        )
                                        .build()
                        )
                        .build()
        );

        if (!command.kitSelection().totalKitCharge().isZero()) {
            paramsBuilder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity((long) command.kitSelection().requestedTotalKits())
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency(command.kitSelection().kitUnitPrice().currency().name().toLowerCase())
                                            .setUnitAmount(toMinorUnits(command.kitSelection().kitUnitPrice().amount()))
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName("uFlex IoT Kits")
                                                            .setDescription("Base kits: %s, additional kits: %s".formatted(
                                                                    command.kitSelection().baseKits(),
                                                                    command.kitSelection().additionalKits()
                                                            ))
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );
        }

        paramsBuilder
                .putMetadata("requested_total_kits", String.valueOf(command.kitSelection().requestedTotalKits()))
                .putMetadata("base_kits", String.valueOf(command.kitSelection().baseKits()))
                .putMetadata("additional_kits", String.valueOf(command.kitSelection().additionalKits()))
                .putMetadata("checkout_total_amount", command.amount().amount().toPlainString());

        var params = paramsBuilder.build();

        try {
            Session session = Session.create(params);
            return new CheckoutSessionResult(session.getId(), session.getUrl());
        } catch (StripeException exception) {
            throw new IllegalStateException("Failed to create Stripe checkout session", exception);
        }
    }

    private long toMinorUnits(BigDecimal amount) {
        return amount
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact();
    }
}
