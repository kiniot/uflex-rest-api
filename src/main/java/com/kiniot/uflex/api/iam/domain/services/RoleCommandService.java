package com.kiniot.uflex.api.iam.domain.services;

import com.kiniot.uflex.api.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {
    void handle(SeedRolesCommand command);
}