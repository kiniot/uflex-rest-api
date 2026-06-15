package com.kiniot.uflex.api.iam.interfaces.rest.controllers;

import com.kiniot.uflex.api.iam.domain.model.aggregates.User;
import com.kiniot.uflex.api.iam.domain.model.entities.Role;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Password;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.RoleName;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.TenantId;
import com.kiniot.uflex.api.iam.domain.services.UserCommandService;
import com.kiniot.uflex.api.shared.interfaces.rest.ApiErrorCodeResolver;
import com.kiniot.uflex.api.shared.interfaces.rest.ErrorResponseFactory;
import com.kiniot.uflex.api.shared.interfaces.rest.GlobalExceptionHandler;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationControllerTests {

    private UserCommandService userCommandService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userCommandService = mock(UserCommandService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthenticationController(userCommandService))
                .setControllerAdvice(new GlobalExceptionHandler(new ErrorResponseFactory(new ApiErrorCodeResolver())))
                .build();
    }

    @Test
    void signInReturnsAuthenticatedUserWithTenantAndJwt() throws Exception {
        var tenantId = UUID.randomUUID();
        var user = new User(
                new Email("patient@example.com"),
                new Password("hashed-password"),
                List.of(new Role(RoleName.ROLE_PATIENT)),
                new TenantId(tenantId)
        );
        when(userCommandService.handle(any(com.kiniot.uflex.api.iam.domain.model.commands.SignInCommand.class)))
                .thenReturn(Optional.of(ImmutablePair.of(user, "jwt-token")));

        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"patient@example.com","password":"secret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("patient@example.com"))
                .andExpect(jsonPath("$.tenantId").value(tenantId.toString()))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void signInReturnsNotFoundWhenCredentialsDoNotResolveAUser() throws Exception {
        when(userCommandService.handle(any(com.kiniot.uflex.api.iam.domain.model.commands.SignInCommand.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"missing@example.com","password":"secret"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void signUpReturnsBadRequestForInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-an-email","password":"secret","roles":["ROLE_PATIENT"]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
