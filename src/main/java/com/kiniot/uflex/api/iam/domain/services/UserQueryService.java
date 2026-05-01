package com.kiniot.uflex.api.iam.domain.services;

import com.kiniot.uflex.api.iam.domain.model.aggregates.User;
import com.kiniot.uflex.api.iam.domain.model.queries.GetUserByEmailQuery;

import java.util.Optional;

public interface UserQueryService {
    Optional<User> handle(GetUserByEmailQuery query);
}