package com.kiniot.uflex.api.shared.interfaces.rest.resources;

public record ErrorResource(
        String detail,
        int status,
        String title,
        String timestamp,
        String path
) {
}
