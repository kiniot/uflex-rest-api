package com.kiniot.uflex.api.subscription.application.internal.queryservices;

import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.domain.model.entities.Invoice;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetInvoiceHistoryQuery;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetSubscriptionByClinicQuery;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetSubscriptionByIdQuery;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionId;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionQueryService;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.InvoiceRepository;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionQueryServiceImpl implements SubscriptionQueryService {
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;

    public SubscriptionQueryServiceImpl(SubscriptionRepository subscriptionRepository, InvoiceRepository invoiceRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public Optional<Subscription> handle(GetSubscriptionByClinicQuery query) {
        return subscriptionRepository.findByClinicId(query.clinicId());
    }

    @Override
    public Optional<Subscription> handle(GetSubscriptionByIdQuery query) {
        return subscriptionRepository.findById(new SubscriptionId(query.subscriptionId()));
    }

    @Override
    public List<Invoice> handle(GetInvoiceHistoryQuery query) {
        return invoiceRepository.findBySubscriptionId(query.subscriptionId());
    }
}
