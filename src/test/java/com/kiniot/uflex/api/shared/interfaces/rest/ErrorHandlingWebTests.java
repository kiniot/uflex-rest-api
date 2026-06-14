package com.kiniot.uflex.api.shared.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiniot.uflex.api.iam.infrastructure.authorization.sfs.pipeline.ForbiddenRequestHandler;
import com.kiniot.uflex.api.iam.infrastructure.authorization.sfs.pipeline.UnauthorizedRequestHandlerEntryPoint;
import com.kiniot.uflex.api.organization.domain.exceptions.CrossClinicAssignmentException;
import com.kiniot.uflex.api.planning.domain.exceptions.PatientWithIdNotFoundException;
import com.kiniot.uflex.api.shared.interfaces.rest.resources.ErrorResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ErrorHandlingWebTests {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ErrorResponseFactory errorResponseFactory = new ErrorResponseFactory(new ApiErrorCodeResolver());
    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler(errorResponseFactory);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler(errorResponseFactory))
                .build();
    }

    @Test
    void shouldReturnCustom401Payload() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test-secured/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();
        var handler = new UnauthorizedRequestHandlerEntryPoint(errorResponseFactory);

        handler.commence(request, response, new BadCredentialsException("Invalid token"));

        ErrorResource error = objectMapper.readValue(response.getContentAsByteArray(), ErrorResource.class);
        assertEquals(401, response.getStatus());
        assertEquals(APPLICATION_JSON_VALUE, response.getContentType());
        assertEquals("AUTH_REQUIRED", error.code());
        assertEquals("Authentication is required to access this resource", error.message());
        assertEquals(401, error.status());
        assertEquals("Unauthorized", error.title());
        assertEquals("/test-secured/admin", error.path());
    }

    @Test
    void shouldReturnCustom403Payload() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test-secured/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();
        var handler = new ForbiddenRequestHandler(errorResponseFactory);

        handler.handle(request, response, new AccessDeniedException("Forbidden"));

        ErrorResource error = objectMapper.readValue(response.getContentAsByteArray(), ErrorResource.class);
        assertEquals(403, response.getStatus());
        assertEquals(APPLICATION_JSON_VALUE, response.getContentType());
        assertEquals("ACCESS_DENIED", error.code());
        assertEquals("You do not have permission to access this resource", error.message());
        assertEquals(403, error.status());
        assertEquals("Forbidden", error.title());
        assertEquals("/test-secured/admin", error.path());
    }

    @Test
    void shouldReturnCustom404Payload() throws Exception {
        mockMvc.perform(get("/test-errors/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PATIENT_WITH_ID_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Patient with id patient-123 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/test-errors/not-found"));
    }

    @Test
    void shouldReturnCustom409Payload() throws Exception {
        mockMvc.perform(get("/test-errors/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CROSS_CLINIC_ASSIGNMENT"))
                .andExpect(jsonPath("$.message").value("Cannot assign patient to a physiotherapist from a different clinic"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.path").value("/test-errors/conflict"));
    }

    @Test
    void shouldReturnClear400ForMethodArgumentTypeMismatch() throws Exception {
        var request = new MockHttpServletRequest("GET", "/test-errors/routines/%7BroutineOrder%7D");
        var exception = new MethodArgumentTypeMismatchException("{routineOrder}", Integer.class, "routineOrder", null, null);

        ResponseEntity<ErrorResource> response = globalExceptionHandler.handleMethodArgumentTypeMismatchException(exception, request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("BAD_REQUEST", response.getBody().code());
        assertEquals("Parameter 'routineOrder' must be a valid integer. Received: \"{routineOrder}\"", response.getBody().message());
        assertEquals("/test-errors/routines/%7BroutineOrder%7D", response.getBody().path());
    }

    @Test
    void shouldReturnClear400ForMissingRequestParameter() throws Exception {
        var request = new MockHttpServletRequest("GET", "/test-errors/missing-param");
        var exception = new MissingServletRequestParameterException("requiredValue", "String");

        ResponseEntity<ErrorResource> response = globalExceptionHandler.handleMissingServletRequestParameterException(exception, request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("BAD_REQUEST", response.getBody().code());
        assertEquals("Required request parameter 'requiredValue' is missing", response.getBody().message());
        assertEquals("/test-errors/missing-param", response.getBody().path());
    }

    @Test
    void shouldReturnClear400ForMalformedRequestBody() throws Exception {
        var request = new MockHttpServletRequest("POST", "/test-errors/malformed-body");
        var exception = new HttpMessageNotReadableException(
                "Malformed JSON",
                new MockHttpInputMessage(new byte[0])
        );

        ResponseEntity<ErrorResource> response = globalExceptionHandler.handleHttpMessageNotReadableException(exception, request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("BAD_REQUEST", response.getBody().code());
        assertEquals("Request body is malformed or contains invalid values", response.getBody().message());
        assertEquals("/test-errors/malformed-body", response.getBody().path());
    }

    @RestController
    static class TestController {

        @GetMapping("/test-errors/not-found")
        String notFound() {
            throw new PatientWithIdNotFoundException("patient-123");
        }

        @GetMapping("/test-errors/conflict")
        String conflict() {
            throw new CrossClinicAssignmentException();
        }
    }
}
