package com.kiniot.uflex.api.iam.domain.services;

import com.kiniot.uflex.api.iam.domain.model.aggregates.User;
import com.kiniot.uflex.api.iam.domain.model.commands.AssignUserRoleCommand;
import com.kiniot.uflex.api.iam.domain.model.commands.AssignUserTenantId;
import com.kiniot.uflex.api.iam.domain.model.commands.ChangePasswordCommand;
import com.kiniot.uflex.api.iam.domain.model.commands.SignInCommand;
import com.kiniot.uflex.api.iam.domain.model.commands.SignUpCommand;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Optional;

public interface UserCommandService {

    Optional<User> handle(SignUpCommand command);

    Optional<ImmutablePair<User, String>> handle(SignInCommand command);

    void handle(AssignUserTenantId command);

    void handle(AssignUserRoleCommand command);

    void handle(ChangePasswordCommand command);
}
