package com.kiniot.uflex.api.iam.domain.model.aggregates;

import com.kiniot.uflex.api.iam.domain.model.entities.Role;
import com.kiniot.uflex.api.iam.domain.model.events.UserCreatedEvent;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Password;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User aggregate root representing an authenticated user entity in the system.
 * <p>
 * This class inherits auditing and persistence state management from
 * {@link AuditableAbstractAggregateRoot}.
 */
@Getter
@Entity
@Table(indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true)
})
public class User extends AuditableAbstractAggregateRoot<User, UserId> {

    /**
     * The unique identifier for this user, represented as an embedded UUID v7 value object.
     * Marked as {@code @EmbeddedId} to indicate this is a composite primary key embedded in the User entity.
     * The UserId value object is used directly as the database primary key.
     */
    @EmbeddedId
    private UserId id;

    @Embedded
    private Email email;

    @Embedded
    private Password password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    protected User() {}

    public User(Email email, Password password) {
        this.id = new UserId();
        this.email = email;
        this.password = password;
        this.roles = new HashSet<>();
    }

    public User(Email email, Password password, List<Role> roles) {
        this(email, password);
        addRoles(roles);
    }

    /**
     * Add a role to the user
     * @param role the role to add
     * @return the user with the added role
     */
    public User addRole(Role role) {
        this.roles.add(role);
        return this;
    }

    /**
     * Add a list of roles to the user
     * @param roles the list of roles to add
     * @return the user with the added roles
     */
    public User addRoles(List<Role> roles) {
        var validatedRoleSet = Role.validateRoleSet(roles);
        this.roles.addAll(validatedRoleSet);
        return this;
    }

    /**
     * Publishes a {@link UserCreatedEvent} domain event.
     */
    public void registerUserCreatedEvent() {
        this.addDomainEvent(
                new UserCreatedEvent(
                        this.id.id().toString(),
                        this.getEmail().email(),
                        this.getRoles().stream().map(role -> role.getName().name()).toList()));
    }

    /**
     * Simply returns the id field to satisfy the Persistable interface
     * defined in the base class.
     */
    @Override
    public UserId getId() {
        return id;
    }
}