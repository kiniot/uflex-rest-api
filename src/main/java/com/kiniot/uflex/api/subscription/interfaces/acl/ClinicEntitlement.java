package com.kiniot.uflex.api.subscription.interfaces.acl;

/**
 * A clinic's current kit entitlement, exposed to other contexts (e.g. the device context
 * to compute the fulfillment queue).
 */
public record ClinicEntitlement(
        String clinicId,
        int requestedTotalKits
) {
}
