package com.kiniot.uflex.api.iam.domain.services;

import com.kiniot.uflex.api.iam.domain.model.aggregates.User;
import com.kiniot.uflex.api.iam.domain.model.queries.GetAuthenticatedUserIdQuery;
import com.kiniot.uflex.api.iam.domain.model.queries.GetAuthenticatedUserTenantIdQuery;
import com.kiniot.uflex.api.iam.domain.model.queries.GetUserByEmailQuery;
import com.kiniot.uflex.api.iam.domain.model.queries.GetUserByIdQuery;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.TenantId;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;

import java.util.Optional;

public interface UserQueryService {
    Optional<User> handle(GetUserByIdQuery query);
    Optional<User> handle(GetUserByEmailQuery query);
    Optional<UserId> handle(GetAuthenticatedUserIdQuery query);
    Optional<TenantId> handle(GetAuthenticatedUserTenantIdQuery query);
}