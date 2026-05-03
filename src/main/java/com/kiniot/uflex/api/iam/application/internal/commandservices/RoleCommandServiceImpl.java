package com.kiniot.uflex.api.iam.application.internal.commandservices;

import com.kiniot.uflex.api.iam.domain.model.commands.SeedRolesCommand;
import com.kiniot.uflex.api.iam.domain.model.entities.Role;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.RoleName;
import com.kiniot.uflex.api.iam.domain.services.RoleCommandService;
import com.kiniot.uflex.api.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class RoleCommandServiceImpl implements RoleCommandService {

    private final RoleRepository roleRepository;

    public RoleCommandServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void handle(SeedRolesCommand command) {
        Arrays.stream(RoleName.values()).forEach(role -> {
            if (!roleRepository.existsByName(role)) {
                roleRepository.save(new Role(RoleName.valueOf(role.name())));
            }
        });
    }
}