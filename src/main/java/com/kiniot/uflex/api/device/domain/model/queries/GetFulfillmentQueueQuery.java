package com.kiniot.uflex.api.device.domain.model.queries;

/**
 * Lists clinics that paid for more kits than they currently own, i.e. the queue of
 * pending hardware to ship/assign. Developer console (ROLE_DEVELOPER) only.
 */
public record GetFulfillmentQueueQuery() {
}
