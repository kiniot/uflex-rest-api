package com.kiniot.uflex.api.iam.domain.model.queries;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;

public record GetUserByIdQuery(UserId userId) {
    public GetUserByIdQuery {
        if (userId == null)
            throw new IllegalArgumentException("User ID cannot be null or less than or equal to zero");
    }
}