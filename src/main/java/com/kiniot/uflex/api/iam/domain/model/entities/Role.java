package com.kiniot.uflex.api.iam.domain.model.entities;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.RoleId;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.RoleName;
import com.kiniot.uflex.api.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.util.List;

/**
 * Role entity representing a user role within the IAM system.
 * <p>
 * Each role maintains its identity through a {@link RoleId} and a {@link RoleName} enum value,
 * and implements {@link Persistable} for proper Spring Data JPA integration.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@With
public class Role extends AuditableModel implements Persistable<RoleId> {

    /**
     * The unique identifier for this role, represented as a UUID v7 value object.
     */
    @EmbeddedId
    private RoleId id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoleName name;

    @Transient
    private boolean isNew = true;

    /**
     * Creates a new Role with the specified name and an auto-generated UUID v7 identifier.
     *
     * @param name the enum-based name of the role
     */
    public Role(RoleName name) {
        this.id = new RoleId();
        this.name = name;
    }

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
     * JPA lifecycle callback that marks the entity as persisted after loading or creation.
     */
    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    /**
     * Returns the role's unique identifier.
     *
     * @return the RoleId value object
     */
    @Override
    public RoleId getId() {
        return id;
    }

    /**
     * Indicates whether this entity is newly created and not yet persisted.
     *
     * @return true if the entity is new, false if it has been persisted
     */
    @Override
    public boolean isNew() {
        return isNew;
    }
}