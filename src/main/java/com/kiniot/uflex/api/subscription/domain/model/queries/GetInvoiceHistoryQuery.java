package com.kiniot.uflex.api.subscription.domain.model.queries;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionId;

public record GetInvoiceHistoryQuery(SubscriptionId subscriptionId) {
}
