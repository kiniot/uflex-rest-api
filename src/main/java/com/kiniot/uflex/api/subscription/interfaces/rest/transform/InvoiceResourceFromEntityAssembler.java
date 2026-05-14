package com.kiniot.uflex.api.subscription.interfaces.rest.transform;

import com.kiniot.uflex.api.subscription.domain.model.entities.Invoice;
import com.kiniot.uflex.api.subscription.interfaces.rest.resources.InvoiceResource;

public class InvoiceResourceFromEntityAssembler {
    public static InvoiceResource toResourceFromEntity(Invoice entity) {
        return new InvoiceResource(
                entity.getId().id(),
                entity.getSubscriptionId(),
                entity.getAmount().amount(),
                entity.getCurrency(),
                entity.getIssuedAt(),
                entity.getDueAt(),
                entity.getPaidAt(),
                entity.getStatus(),
                entity.getProviderTransactionId()
        );
    }
}
