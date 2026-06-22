package com.kiniot.uflex.api.subscription.application.acl;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionStatus;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import com.kiniot.uflex.api.subscription.interfaces.acl.ClinicEntitlement;
import com.kiniot.uflex.api.subscription.interfaces.acl.SubscriptionContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    @Transactional(readOnly = true)
    public List<ClinicEntitlement> getCurrentEntitlements() {
        var today = LocalDate.now();
        // One entitlement per clinic: keep the highest requested kits among its current subscriptions.
        Map<String, Integer> byClinic = subscriptionRepository
                .findAllByStatusIn(List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE)).stream()
                .filter(subscription -> subscription.isCurrentAt(today))
                .filter(subscription -> subscription.getClinicId() != null && subscription.getClinicId().id() != null)
                .filter(subscription -> subscription.getKitSelection() != null)
                .collect(Collectors.toMap(
                        subscription -> subscription.getClinicId().id().toString(),
                        subscription -> {
                            var kits = subscription.getKitSelection().requestedTotalKits();
                            return kits == null ? 0 : kits;
                        },
                        Integer::max));
        return byClinic.entrySet().stream()
                .map(entry -> new ClinicEntitlement(entry.getKey(), entry.getValue()))
                .toList();
    }
}
