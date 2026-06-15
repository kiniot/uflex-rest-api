package com.kiniot.uflex.api.iam.domain.model.aggregates;

import com.kiniot.uflex.api.iam.domain.model.entities.Role;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Password;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.RoleName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTests {

    @Test
    void removeRoleByNameRemovesOnlyMatchingRole() {
        var user = new User(
                new Email("clinic-admin@example.com"),
                new Password("hashed-password"),
                List.of(new Role(RoleName.ROLE_USER), new Role(RoleName.ROLE_CLINIC_ADMIN))
        );

        user.removeRoleByName(RoleName.ROLE_USER.name());

        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().stream().anyMatch(role -> role.getName().equals(RoleName.ROLE_CLINIC_ADMIN)));
    }
}
