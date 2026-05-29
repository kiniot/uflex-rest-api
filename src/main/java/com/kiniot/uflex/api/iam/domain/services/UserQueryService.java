package com.kiniot.uflex.api.iam.domain.services;

import com.kiniot.uflex.api.iam.domain.model.aggregates.User;
import com.kiniot.uflex.api.iam.domain.model.queries.*;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.TenantId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;

import java.util.Optional;

public interface UserQueryService {
    Optional<User> handle(GetUserByIdQuery query);
    Optional<User> handle(GetUserByEmailQuery query);
    Optional<UserId> handle(GetContextUserIdQuery query);
    Optional<TenantId> handle(GetContextTenantIdQuery query);
    Optional<UserId> handle(GetCurrentUserIdQuery query);
    Optional<TenantId> handle(GetCurrentTenantIdQuery query);
}
