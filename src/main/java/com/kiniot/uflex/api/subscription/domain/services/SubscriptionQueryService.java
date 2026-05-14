package com.kiniot.uflex.api.subscription.domain.services;

import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.domain.model.entities.Invoice;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetInvoiceHistoryQuery;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetSubscriptionByClinicQuery;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetSubscriptionByIdQuery;

import java.util.List;
import java.util.Optional;

public interface SubscriptionQueryService {
    Optional<Subscription> handle(GetSubscriptionByClinicQuery query);
    Optional<Subscription> handle(GetSubscriptionByIdQuery query);
    List<Invoice> handle(GetInvoiceHistoryQuery query);
}
