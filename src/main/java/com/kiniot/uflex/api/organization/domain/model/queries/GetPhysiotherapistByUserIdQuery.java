package com.kiniot.uflex.api.organization.domain.model.queries;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;

public record GetPhysiotherapistByUserIdQuery(
        UserId userId
) {
    public GetPhysiotherapistByUserIdQuery {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }
}
