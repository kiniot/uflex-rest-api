package com.kiniot.uflex.api.iam.interfaces.rest.controllers;

import com.kiniot.uflex.api.iam.domain.services.UserCommandService;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.SignInResource;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.SignUpResource;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.UserResource;
import com.kiniot.uflex.api.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.kiniot.uflex.api.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.kiniot.uflex.api.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.kiniot.uflex.api.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Available Authentication Endpoints")
public class AuthenticationController {

    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @PostMapping("/sign-up")
    @Operation(summary = "Sign-up", description = "Create a new user account with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
    })
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource resource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
        var user = userCommandService.handle(signUpCommand);
        if (user.isEmpty()) return ResponseEntity.badRequest().build();
        var userEntity = user.get();
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(userEntity);
        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Sign-in", description = "Authenticate a user with the provided credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
    })
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource resource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
        var authenticatedUser = userCommandService.handle(signInCommand);
        if (authenticatedUser.isEmpty())
            return ResponseEntity.notFound().build();
        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(authenticatedUser.get().getLeft(), authenticatedUser.get().getRight());
        return ResponseEntity.ok(authenticatedUserResource);
    }
}
