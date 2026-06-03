package com.kiniot.uflex.api.iam.domain.model.commands;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;

public record UpdateUserEmailCommand(
        UserId userId,
        Email email
) {
    public UpdateUserEmailCommand {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
    }
}
