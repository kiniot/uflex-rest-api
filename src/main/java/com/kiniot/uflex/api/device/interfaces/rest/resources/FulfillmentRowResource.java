package com.kiniot.uflex.api.device.interfaces.rest.resources;

public record FulfillmentRowResource(
        String clinicId,
        String clinicName,
        int requested,
        int owned,
        int pending
) {
}
