package com.kiniot.uflex.api.iam.domain.model.commands;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.RoleName;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;

public record AssignUserRoleCommand(
        UserId userId,
        RoleName roleName
) {
}
