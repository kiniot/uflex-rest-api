package com.kiniot.uflex.api.device.domain.model.queries;

/**
 * One clinic's fulfillment status: kits it paid for vs kits it owns, and the shortfall.
 */
public record FulfillmentRow(
        String clinicId,
        String clinicName,
        int requested,
        int owned,
        int pending
) {
}
