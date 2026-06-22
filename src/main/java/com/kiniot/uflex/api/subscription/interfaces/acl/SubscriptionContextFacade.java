package com.kiniot.uflex.api.subscription.interfaces.acl;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;

import java.util.List;

/**
 * Anti-corruption facade exposing read-only subscription data to other bounded contexts.
 */
public interface SubscriptionContextFacade {

    /**
     * Total kits the clinic's currently-valid subscription paid for, or {@code 0} when the
     * clinic has no current subscription. Used by the device context to derive how many
     * kits are still pending shipment.
     */
    int getRequestedTotalKitsByClinicId(ClinicId clinicId);

    /**
     * Kit entitlements for every clinic with a current (ACTIVE/PAST_DUE) subscription.
     * Used by the device context to build the fulfillment queue.
     */
    List<ClinicEntitlement> getCurrentEntitlements();
}
