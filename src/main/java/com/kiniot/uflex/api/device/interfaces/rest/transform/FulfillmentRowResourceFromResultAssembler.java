package com.kiniot.uflex.api.device.interfaces.rest.transform;

import com.kiniot.uflex.api.device.domain.model.queries.FulfillmentRow;
import com.kiniot.uflex.api.device.interfaces.rest.resources.FulfillmentRowResource;

public class FulfillmentRowResourceFromResultAssembler {

    private FulfillmentRowResourceFromResultAssembler() {}

    public static FulfillmentRowResource toResourceFromResult(FulfillmentRow row) {
        return new FulfillmentRowResource(
                row.clinicId(),
                row.clinicName(),
                row.requested(),
                row.owned(),
                row.pending());
    }
}
