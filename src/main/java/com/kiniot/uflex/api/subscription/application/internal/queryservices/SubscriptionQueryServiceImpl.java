package com.kiniot.uflex.api.subscription.application.internal.queryservices;

import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.domain.model.entities.Invoice;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetInvoiceHistoryQuery;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetSubscriptionByClinicQuery;
import com.kiniot.uflex.api.subscription.domain.model.queries.GetSubscriptionByIdQuery;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionStatus;
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
        var activeSubscription = subscriptionRepository.findByClinicIdAndStatus(query.clinicId().id(), SubscriptionStatus.ACTIVE);
        if (activeSubscription.isPresent()) return activeSubscription;
        return subscriptionRepository.findPaidSubscriptionsByClinicId(query.clinicId().id()).stream().findFirst()
                .map(this::activateRecoveredPaidSubscription);
    }

    private Subscription activateRecoveredPaidSubscription(Subscription subscription) {
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE && subscription.getPaymentReference() != null) {
            var periodStart = subscription.getCurrentPeriodStart() == null
                    ? java.time.OffsetDateTime.now()
                    : subscription.getCurrentPeriodStart();
            var periodEnd = subscription.getCurrentPeriodEnd() == null
                    ? (subscription.getBillingCycle() == com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle.YEARLY
                    ? periodStart.plusYears(1)
                    : periodStart.plusMonths(1))
                    : subscription.getCurrentPeriodEnd();
            subscription.refreshStripePayment(subscription.getPaymentReference(), periodStart, periodEnd);
            return subscriptionRepository.save(subscription);
        }
        return subscription;
    }

    @Override
    public Optional<Subscription> handle(GetSubscriptionByIdQuery query) {
        return subscriptionRepository.findById(query.subscriptionId());
    }

    @Override
    public List<Invoice> handle(GetInvoiceHistoryQuery query) {
        return invoiceRepository.findBySubscriptionId(query.subscriptionId().id());
    }
}
