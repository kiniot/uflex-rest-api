package com.kiniot.uflex.api.iam.domain.services;

import com.kiniot.uflex.api.iam.domain.model.aggregates.User;
import com.kiniot.uflex.api.iam.domain.model.commands.*;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Optional;

public interface UserCommandService {

    Optional<User> handle(SignUpCommand command);

    Optional<ImmutablePair<User, String>> handle(SignInCommand command);

    Optional<User> handle(SignUpVerifiedUserCommand command);

    void handle(AssignUserTenantId command);

    void handle(AssignUserRoleCommand command);

    void handle(ChangePasswordCommand command);

    void handle(DeleteUserCommand command);
}
