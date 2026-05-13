package com.kiniot.uflex.api.subscription.interfaces.rest.transform;

import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.PaymentReferenceResource;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.SubscriptionResource;

public class SubscriptionResourceFromEntityAssembler {
    public static SubscriptionResource toResourceFromEntity(Subscription entity) {
        var paymentReference = entity.getPaymentReference();
        var paymentResource = paymentReference == null
                ? null
                : new PaymentReferenceResource(
                paymentReference.provider(),
                paymentReference.providerTransactionId(),
                paymentReference.last4(),
                paymentReference.expiresOn());
        return new SubscriptionResource(
                entity.getId().id(),
                entity.getClinicId().id(),
                PlanResourceFromEntityAssembler.toResourceFromEntity(entity.getPlan()),
                entity.getStatus(),
                entity.getBillingCycle(),
                entity.getCurrentPeriodStart(),
                entity.getCurrentPeriodEnd(),
                entity.getNextBillingDate(),
                entity.getTrialUntil(),
                paymentResource
        );
    }
}
