package com.kiniot.uflex.api.iam.interfaces.rest.controllers;

import com.kiniot.uflex.api.iam.domain.model.queries.GetUserByEmailQuery;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.iam.domain.services.UserQueryService;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.UserResource;
import com.kiniot.uflex.api.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Available User Endpoints")
public class UsersController {

    private final UserQueryService userQueryService;

    public UsersController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping(value = "/{email:.+}")
    @Operation(summary = "Get user by email", description = "Get the user with the specified email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
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
