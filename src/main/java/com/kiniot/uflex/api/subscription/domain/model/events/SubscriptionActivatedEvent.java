package com.kiniot.uflex.api.subscription.domain.model.events;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Raised when a subscription becomes ACTIVE (payment confirmed). Carries the clinic and
 * the total number of kits the clinic paid for, so the device context can assign that
 * many stock devices to the clinic.
 */
@Getter
public class SubscriptionActivatedEvent extends ApplicationEvent {

    private final ClinicId clinicId;
    private final Integer requestedTotalKits;

    public SubscriptionActivatedEvent(Object source, ClinicId clinicId, Integer requestedTotalKits) {
        super(source);
        this.clinicId = clinicId;
        this.requestedTotalKits = requestedTotalKits;
    }
}
