package com.kiniot.uflex.api.subscription.application.internal.queryservices;

import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.domain.model.commands.GetCurrentSubscriptionQuery;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionStatus;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionQueryService;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class SubscriptionQueryServiceImpl implements SubscriptionQueryService {

    private final SubscriptionRepository subscriptionRepository;
    private final ExternalIamService externalIamService;

    public SubscriptionQueryServiceImpl(
            SubscriptionRepository subscriptionRepository,
            ExternalIamService externalIamService
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    public Optional<Subscription> handle(GetCurrentSubscriptionQuery query) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        return subscriptionRepository.findAllByClinicId(clinicId).stream()
                .filter(this::isCurrentSubscription)
                .findFirst();
    }

    private boolean isCurrentSubscription(Subscription subscription) {
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE
                || subscription.getStatus() == SubscriptionStatus.PAST_DUE)
            return true;
        return subscription.getStatus() == SubscriptionStatus.CANCELED
                && subscription.getEndsAt() != null
                && !subscription.getEndsAt().isBefore(LocalDate.now());
    }
}
