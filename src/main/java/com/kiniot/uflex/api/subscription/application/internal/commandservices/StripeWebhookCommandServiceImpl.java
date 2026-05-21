package com.kiniot.uflex.api.subscription.application.internal.commandservices;

import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionStatus;
import com.kiniot.uflex.api.subscription.domain.services.StripeWebhookCommandService;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class StripeWebhookCommandServiceImpl implements StripeWebhookCommandService {

    private final SubscriptionRepository subscriptionRepository;

    public StripeWebhookCommandServiceImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    @Transactional
    public void handleCheckoutCompleted(String checkoutSessionId) {
        var subscription = subscriptionRepository.findByCheckoutSessionId(checkoutSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found for checkout session"));
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) return;
        subscription.activate();
        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void handleCheckoutExpired(String checkoutSessionId) {
        var subscription = subscriptionRepository.findByCheckoutSessionId(checkoutSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found for checkout session"));
        subscription.expirePendingCheckout();
        subscriptionRepository.save(subscription);
    }
}
