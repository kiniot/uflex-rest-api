package com.kiniot.uflex.api.subscription.domain.services;

import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.domain.model.commands.CancelSubscriptionCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.ChangeSubscriptionPlanCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.CompleteCheckoutSessionPaymentCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.ConfirmCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.CreateChangePlanCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.CreateSubscriptionCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.PurchaseSubscriptionPlanCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.RegisterInvoicePaymentCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.UpdatePaymentMethodCommand;
import com.kiniot.uflex.api.subscription.domain.services.results.CheckoutSessionResult;

import java.util.Optional;

public interface SubscriptionCommandService {
    CheckoutSessionResult handle(CreateSubscriptionCheckoutSessionCommand command);
    CheckoutSessionResult handle(CreateChangePlanCheckoutSessionCommand command);
    Optional<Subscription> handle(ConfirmCheckoutSessionCommand command);
    Optional<Subscription> handle(CompleteCheckoutSessionPaymentCommand command);
    Optional<Subscription> handle(PurchaseSubscriptionPlanCommand command);
    Optional<Subscription> handle(ChangeSubscriptionPlanCommand command);
    Optional<Subscription> handle(CancelSubscriptionCommand command);
    Optional<Subscription> handle(UpdatePaymentMethodCommand command);
    void handle(RegisterInvoicePaymentCommand command);
}
