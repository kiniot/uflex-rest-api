package com.kiniot.uflex.api.iam.infrastructure.authorization.sfs.pipeline;

import com.kiniot.uflex.api.shared.interfaces.rest.ErrorResponseFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UnauthorizedRequestHandlerEntryPoint implements AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnauthorizedRequestHandlerEntryPoint.class);
    private final ErrorResponseFactory errorResponseFactory;

    public UnauthorizedRequestHandlerEntryPoint(ErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException) throws IOException, ServletException {
        LOGGER.error("Unauthorized request: {}", authenticationException.getMessage());
        var errorResource = errorResponseFactory.build(
                HttpStatus.UNAUTHORIZED,
                "Authentication is required to access this resource",
                request,
                authenticationException
        );
        ProblemDetailResponseWriter.write(response, errorResource);
    }
}
