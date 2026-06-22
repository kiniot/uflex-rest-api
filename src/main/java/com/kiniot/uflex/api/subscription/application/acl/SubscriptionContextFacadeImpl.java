package com.kiniot.uflex.api.subscription.application.acl;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import com.kiniot.uflex.api.subscription.interfaces.acl.SubscriptionContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class SubscriptionContextFacadeImpl implements SubscriptionContextFacade {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionContextFacadeImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public int getRequestedTotalKitsByClinicId(ClinicId clinicId) {
        var today = LocalDate.now();
        return subscriptionRepository.findAllByClinicId(clinicId).stream()
                .filter(subscription -> subscription.isCurrentAt(today))
                .map(Subscription::getKitSelection)
                .filter(java.util.Objects::nonNull)
                .mapToInt(kitSelection -> kitSelection.requestedTotalKits() == null ? 0 : kitSelection.requestedTotalKits())
                .max()
                .orElse(0);
    }
}
