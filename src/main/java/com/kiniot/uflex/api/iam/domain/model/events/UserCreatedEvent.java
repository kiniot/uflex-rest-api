package com.kiniot.uflex.api.iam.domain.model.events;

import java.util.List;

public record UserCreatedEvent(
        String id,
        String email,
        List<String> roles
) {
}