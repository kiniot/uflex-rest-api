package com.kiniot.uflex.api.shared.interfaces.rest;

import com.kiniot.uflex.api.iam.domain.exceptions.UserWithIdNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErrorResponseFactoryTests {

    private final ErrorResponseFactory factory = new ErrorResponseFactory(new ApiErrorCodeResolver());

    @Test
    void shouldBuildErrorResourceWithExpectedFields() {
        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/me");
        var exception = new UserWithIdNotFoundException("123");

        var errorResource = factory.build(HttpStatus.NOT_FOUND, exception.getMessage(), request, exception);

        assertEquals("USER_WITH_ID_NOT_FOUND", errorResource.code());
        assertEquals("User with ID 123 not found", errorResource.message());
        assertEquals(404, errorResource.status());
        assertEquals("Not Found", errorResource.title());
        assertEquals("/api/v1/users/me", errorResource.path());
        assertNotNull(errorResource.timestamp());
    }

    @Test
    void shouldExposeErrorResourceFieldsInContractOrder() {
        String[] componentNames = java.util.Arrays.stream(com.kiniot.uflex.api.shared.interfaces.rest.resources.ErrorResource.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName)
                .toArray(String[]::new);

        assertArrayEquals(new String[]{"code", "message", "status", "title", "timestamp", "path"}, componentNames);
    }
}
