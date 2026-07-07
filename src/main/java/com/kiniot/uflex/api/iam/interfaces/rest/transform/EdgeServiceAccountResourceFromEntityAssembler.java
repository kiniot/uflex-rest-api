package com.kiniot.uflex.api.iam.interfaces.rest.transform;

import com.kiniot.uflex.api.iam.domain.model.aggregates.EdgeServiceAccount;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.EdgeServiceAccountResource;

public class EdgeServiceAccountResourceFromEntityAssembler {

    private EdgeServiceAccountResourceFromEntityAssembler() {}

    public static EdgeServiceAccountResource toResourceFromEntity(EdgeServiceAccount account) {
        var clinicId = account.getTenantId() != null && account.getTenantId().tenantId() != null
                ? account.getTenantId().tenantId().toString()
                : null;
        var createdAt = account.getCreatedAt() != null
                ? account.getCreatedAt().toInstant().toString()
                : null;
        return new EdgeServiceAccountResource(
                account.getId().id().toString(),
                account.getSerialNumber(),
                clinicId,
                createdAt);
    }
}
