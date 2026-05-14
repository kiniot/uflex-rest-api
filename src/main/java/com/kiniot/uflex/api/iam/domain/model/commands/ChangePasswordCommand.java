package com.kiniot.uflex.api.iam.domain.model.commands;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.Password;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;

public record ChangePasswordCommand(
        UserId userId,
        Password currentPassword,
        Password newPassword
) {
    public ChangePasswordCommand {
        if (userId == null) throw new IllegalArgumentException("User ID cannot be null");
        if (currentPassword == null) throw new IllegalArgumentException("Current password cannot be null");
        if (newPassword == null) throw new IllegalArgumentException("New password cannot be null");
        if (currentPassword.password().equals(newPassword.password()))
            throw new IllegalArgumentException("New password must be different from current password");
    }
}
