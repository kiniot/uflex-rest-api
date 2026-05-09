package com.kiniot.uflex.api.iam.domain.model.aggregates;

import com.kiniot.uflex.api.iam.domain.model.entities.Role;
import com.kiniot.uflex.api.iam.domain.model.events.UserCreatedEvent;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Password;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.TenantId;
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

    /**
     * The tenant identifier that associates this user with a specific tenant in a multi-tenant system.
     * <p>
     * Represented as an embedded value object and can be modified through the
     * {@link #associateTenant(TenantId)} and {@link #disassociateTenant(TenantId)} methods.
     */
    @Embedded
    private TenantId tenantId;

    protected User() {}

    public User(Email email, Password password) {
        this.id = new UserId();
        this.email = email;
        this.password = password;
        this.roles = new HashSet<>();
        this.tenantId = new TenantId();
    }

    public User(Email email, Password password, List<Role> roles) {
        this(email, password);
        addRoles(roles);
    }

    public User(Email email, Password password, List<Role> roles, TenantId tenantId) {
        this(email, password, roles);
        this.tenantId = tenantId;
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
     * Replaces the user's password with the supplied (already hashed) one.
     * Hashing and current-password verification must be done by the caller.
     */
    public void changePassword(Password newHashedPassword) {
        this.password = newHashedPassword;
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
     * Associates this user with a specific tenant.
     * <p>
     * A user can only be assigned to a tenant once. If the user is already associated with a tenant,
     * this method will throw an exception.
     *
     * @param tenantId the tenant identifier to associate with this user
     * @throws IllegalStateException if the user is already associated with a tenant
     */
    public void associateTenant(TenantId tenantId) {
        if (this.tenantId != null && this.tenantId.isAssigned())
            throw new IllegalStateException("User is already associated with a tenant");
        this.tenantId = tenantId;
    }

    /**
     * Disassociates this user from a specific tenant.
     * <p>
     * This method validates that the user is currently associated with the provided tenant
     * before removing the association.
     *
     * @param tenantId the tenant identifier to disassociate from
     * @throws IllegalStateException if the user is not associated with the provided tenant
     */
    public void disassociateTenant(TenantId tenantId) {
        if (this.tenantId == null || !this.tenantId.equals(tenantId)) {
            throw new IllegalStateException("User is not associated with the provided tenant");
        }
        this.tenantId = new TenantId();
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