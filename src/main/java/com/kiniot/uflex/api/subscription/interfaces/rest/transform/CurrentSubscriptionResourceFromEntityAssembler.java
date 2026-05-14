package com.kiniot.uflex.api.subscription.interfaces.rest.transform;

import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.CurrentSubscriptionResource;

public class CurrentSubscriptionResourceFromEntityAssembler {
    public static CurrentSubscriptionResource toResourceFromEntity(Subscription entity) {
        var plan = entity.getPlan();
        var amount = entity.getBillingCycle() == BillingCycle.YEARLY
                ? plan.getYearlyPrice().amount()
                : plan.getMonthlyPrice().amount();
        var paymentReference = entity.getPaymentReference();
        var paymentResource = paymentReference == null
                ? null
                : new com.kiniot.uflex.api.subscription.interfaces.rest.resources.PaymentReferenceResource(
                paymentReference.provider(),
                paymentReference.providerTransactionId(),
                paymentReference.providerCheckoutSessionId(),
                paymentReference.providerCustomerId(),
                paymentReference.providerSubscriptionId(),
                paymentReference.last4(),
                paymentReference.expiresOn());
        return new CurrentSubscriptionResource(
                entity.getId().id(),
                entity.getClinicId().id(),
                plan.getId().id(),
                plan.getName(),
                entity.getStatus(),
                entity.getBillingCycle(),
                amount,
                plan.getCurrency(),
                entity.getCurrentPeriodStart(),
                entity.getCurrentPeriodEnd(),
                entity.getNextBillingDate(),
                paymentResource
        );
    }
}
