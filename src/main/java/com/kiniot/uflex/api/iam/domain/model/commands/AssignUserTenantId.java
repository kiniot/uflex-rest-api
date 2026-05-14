package com.kiniot.uflex.api.iam.domain.model.commands;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.TenantId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;

public record AssignUserTenantId(
        UserId userId,
        TenantId tenantId
) {
}
