package com.kiniot.uflex.api.subscription.application.internal.commandservices;

import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.domain.model.commands.CreateSubscriptionCommand;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionCommandService;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.TierRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class SubscriptionCommandServiceImpl implements SubscriptionCommandService {

    private final SubscriptionRepository subscriptionRepository;
    private final TierRepository tierRepository;
    private final ExternalIamService externalIamService;

    public SubscriptionCommandServiceImpl(
            SubscriptionRepository subscriptionRepository,
            ExternalIamService externalIamService,
            TierRepository tierRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.externalIamService = externalIamService;
        this.tierRepository = tierRepository;
    }

    @Override
    public Optional<Subscription> handle(CreateSubscriptionCommand command) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        var hasCurrentSubscription = subscriptionRepository.findAllByClinicId(clinicId).stream()
                .anyMatch(subscription -> subscription.isCurrentAt(LocalDate.now()));
        if (hasCurrentSubscription) throw new IllegalStateException("Current clinic already has a current subscription");
        var tier = tierRepository.findById(command.selection().tierId())
                .orElseThrow(() -> new IllegalArgumentException("Tier not found"));
        var catalogPrice = tier.getPrice(command.selection().billingPeriod(), command.contractedPrice().currency());
        if (!tier.isAllowsPriceOverride() && !catalogPrice.equals(command.contractedPrice()))
            throw new IllegalArgumentException("Contracted price must match the tier catalog price");
        var subscription = new Subscription(command, clinicId);
        return Optional.of(subscriptionRepository.save(subscription));
    }
}
