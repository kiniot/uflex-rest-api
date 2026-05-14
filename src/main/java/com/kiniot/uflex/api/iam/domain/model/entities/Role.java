package com.kiniot.uflex.api.iam.domain.model.entities;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.RoleId;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.RoleName;
import com.kiniot.uflex.api.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Role entity representing a user role within the IAM system.
 * <p>
 * This entity inherits auditing capabilities and optimized persistence state
 * management from {@link AuditableModel}.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@With
public class Role extends AuditableModel<RoleId> {

    /**
     * The unique identifier for this role, represented as a UUID v7 value object.
     */
    @EmbeddedId
    private RoleId id;

    /**
     * The name of the role stored as a string in the database.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoleName name;

    /**
     * Creates a new Role with the specified name and an auto-generated UUID v7 identifier.
     *
     * @param name the enum-based name of the role
     */
    public Role(RoleName name) {
        this.id = new RoleId();
        this.name = name;
    }

    /**
     * Returns the string representation of the role name.
     *
     * @return the name of the role as a String
     */
    public String getStringName() {
        return name.name();
    }

    /**
     * Returns the default role for newly created users.
     *
     * @return a Role instance with {@link RoleName#ROLE_USER}
     */
    public static Role getDefaultRole() {
        return new Role(RoleName.ROLE_USER);
    }

    /**
     * Converts a string name to a Role instance.
     *
     * @param name the name of the role
     * @return a new Role instance
     */
    public static Role toRoleFromName(String name) {
        return new Role(RoleName.valueOf(name));
    }

    /**
     * Validates a role collection, returning the default role if the input is null or empty.
     *
     * @param roles the role list to validate
     * @return a non-empty role list
     */
    public static List<Role> validateRoleSet(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of(getDefaultRole());
        }
        return roles;
    }

    /**
     * Returns the role's unique identifier.
     * Satisfies the contract defined in {@link AuditableModel}.
     *
     * @return the RoleId value object
     */
    @Override
    public RoleId getId() {
        return id;
    }
}