package com.kiniot.uflex.api.iam.domain.model.commands;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;

public record DeleteUserCommand(UserId userId) {
    public DeleteUserCommand {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }
}
