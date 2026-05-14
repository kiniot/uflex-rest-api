package com.kiniot.uflex.api.iam.application.acl;

import com.kiniot.uflex.api.iam.application.internal.outboundservices.verification.VerificationService;
import com.kiniot.uflex.api.iam.domain.model.entities.Role;
import com.kiniot.uflex.api.iam.domain.model.queries.GetCurrentTenantIdQuery;
import com.kiniot.uflex.api.iam.domain.model.queries.GetAuthenticatedUserIdQuery;
import com.kiniot.uflex.api.iam.domain.model.queries.GetAuthenticatedUserTenantIdQuery;
import com.kiniot.uflex.api.iam.domain.model.queries.GetUserByIdQuery;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.iam.domain.model.commands.DeleteUserCommand;
import com.kiniot.uflex.api.iam.domain.services.UserCommandService;
import com.kiniot.uflex.api.iam.domain.services.UserQueryService;
import com.kiniot.uflex.api.iam.interfaces.acl.IamContextFacade;
import com.kiniot.uflex.api.iam.domain.model.commands.SignUpVerifiedUserCommand;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class IamContextFacadeImpl implements IamContextFacade {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final VerificationService verificationService;

    public IamContextFacadeImpl(
            UserCommandService userCommandService,
            UserQueryService userQueryService,
            VerificationService verificationService
    ) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.verificationService = verificationService;
    }

    @Override
    public String fetchUserEmailAddressByUserId(String userId) {
        var getUserByIdQuery = new GetUserByIdQuery(new UserId(UUID.fromString(userId)));
        var user = userQueryService.handle(getUserByIdQuery);
        return user.map(u -> u.getEmail().email()).orElse("");
    }

    @Override
    public String signUpVerifiedUser(String email, List<String> roles) {
        var randomPassword = verificationService.generateRandomPassword();
        var signUpUserCommand = new SignUpVerifiedUserCommand(
                new Email(email),
                randomPassword,
                roles.stream().map(Role::toRoleFromName).toList());
        var signedUpUser = userCommandService.handle(signUpUserCommand)
                .orElseThrow(() -> new IllegalStateException("User sign up failed"));
        return java.util.Optional.ofNullable(signedUpUser.getId())
                .map(UserId::id)
                .map(UUID::toString)
                .orElseThrow(() -> new IllegalStateException("User ID is null"));
    }

    @Override
    public void deleteUserById(String userId) {
        userCommandService.handle(new DeleteUserCommand(new UserId(UUID.fromString(userId))));
    }

    @Override
    public String fetchAuthenticatedUserId() {
        var getAuthenticatedUserIdQuery = new GetAuthenticatedUserIdQuery();
        return userQueryService.handle(getAuthenticatedUserIdQuery)
                .map(userId -> userId.id().toString())
                .orElseThrow(() -> new IllegalStateException("User fetch authenticated failed"));
    }

    @Override
    public String fetchAuthenticatedUserTenantId() {
        var getAuthenticatedUserTenantIdQuery = new GetAuthenticatedUserTenantIdQuery();
        var tenantId = userQueryService.handle(getAuthenticatedUserTenantIdQuery);
        return tenantId.isEmpty() ? "" : tenantId.get().tenantId().toString();
    }

    @Override
    public String fetchCurrentTenantId() {
        var tenantId = userQueryService.handle(new GetCurrentTenantIdQuery());
        return tenantId.isEmpty() ? "" : tenantId.get().tenantId().toString();
    }
}
