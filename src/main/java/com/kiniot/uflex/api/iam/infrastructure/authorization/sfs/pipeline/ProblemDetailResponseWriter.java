package com.kiniot.uflex.api.iam.infrastructure.authorization.sfs.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiniot.uflex.api.shared.interfaces.rest.resources.ErrorResource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;

public final class ProblemDetailResponseWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ProblemDetailResponseWriter() {
    }

    public static void write(HttpServletResponse response, ErrorResource errorResource) throws IOException {
        response.setStatus(errorResource.status());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OBJECT_MAPPER.writeValue(response.getOutputStream(), errorResource);
    }
}
