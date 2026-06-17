package com.kiniot.uflex.api.shared.interfaces.rest.resources;

public record ErrorResource(
        String code,
        String message,
        int status,
        String title,
        String timestamp,
        String path
) {
}
