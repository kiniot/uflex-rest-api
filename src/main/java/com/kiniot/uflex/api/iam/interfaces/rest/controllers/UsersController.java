package com.kiniot.uflex.api.iam.interfaces.rest.controllers;

import com.kiniot.uflex.api.iam.domain.model.commands.ChangePasswordCommand;
import com.kiniot.uflex.api.iam.domain.model.queries.GetAuthenticatedUserIdQuery;
import com.kiniot.uflex.api.iam.domain.model.queries.GetUserByEmailQuery;
import com.kiniot.uflex.api.iam.domain.model.queries.GetUserByIdQuery;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Password;
import com.kiniot.uflex.api.iam.domain.services.UserCommandService;
import com.kiniot.uflex.api.iam.domain.services.UserQueryService;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.ChangePasswordResource;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.UserResource;
import com.kiniot.uflex.api.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Available User Endpoints")
public class UsersController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    public UsersController(UserQueryService userQueryService, UserCommandService userCommandService) {
        this.userQueryService = userQueryService;
        this.userCommandService = userCommandService;
    }

    @GetMapping(value = "/me")
    @Operation(summary = "Get current authenticated user",
            description = "Returns the profile of the user identified by the Bearer token in the Authorization header")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current user retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Authenticated user no longer exists")
    })
    public ResponseEntity<UserResource> getCurrentUser() {
        var authenticatedUserId = userQueryService.handle(new GetAuthenticatedUserIdQuery());
        if (authenticatedUserId.isEmpty())
            return ResponseEntity.status(401).build();
        var user = userQueryService.handle(new GetUserByIdQuery(authenticatedUserId.get()));
        if (user.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(user.get()));
    }

    @PutMapping(value = "/me/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Change current user's password",
            description = "Updates the password of the authenticated user. Requires the current password for verification.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or current password is incorrect"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — missing or invalid token")
    })
    public ResponseEntity<Void> changeMyPassword(@RequestBody ChangePasswordResource resource) {
        var authenticatedUserId = userQueryService.handle(new GetAuthenticatedUserIdQuery());
        if (authenticatedUserId.isEmpty())
            return ResponseEntity.status(401).build();
        try {
            userCommandService.handle(new ChangePasswordCommand(
                    authenticatedUserId.get(),
                    new Password(resource.currentPassword()),
                    new Password(resource.newPassword())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{email:.+}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #email == authentication.principal.email")
    @Operation(summary = "Get user by email",
            description = "Get the user with the specified email address. Only the owner of the email or an ADMIN can access this resource.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — caller is not the owner or an ADMIN"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResource> getUserByEmail(@PathVariable String email) {
        var getUserByEmailQuery = new GetUserByEmailQuery(new Email(email));
        var user = userQueryService.handle(getUserByEmailQuery);
        if (user.isEmpty())
            return ResponseEntity.notFound().build();
        var userEntity = user.get();
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(userEntity);
        return ResponseEntity.ok(userResource);
    }
}
