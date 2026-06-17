package com.kiniot.uflex.api.iam.infrastructure.authorization.sfs.pipeline;

import com.kiniot.uflex.api.shared.interfaces.rest.ErrorResponseFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ForbiddenRequestHandler implements AccessDeniedHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForbiddenRequestHandler.class);
    private final ErrorResponseFactory errorResponseFactory;

    public ForbiddenRequestHandler(ErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        LOGGER.error("Forbidden request: {}", accessDeniedException.getMessage());
        var errorResource = errorResponseFactory.build(
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource",
                request,
                accessDeniedException
        );
        ProblemDetailResponseWriter.write(response, errorResource);
    }
}
