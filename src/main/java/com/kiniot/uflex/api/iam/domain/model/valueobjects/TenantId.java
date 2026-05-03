package com.kiniot.uflex.api.iam.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

/**
 * Value object representing the tenant identifier assigned to a user.
 * <p>
 * The identifier is stored as a {@link UUID} so it matches the rest of the domain model
 * and can be used as an embedded JPA value object. A {@code null} value means the user
 * is not currently associated with any tenant.
 *
 * @param tenantId the tenant UUID, or {@code null} when unassigned
 */
@Embeddable
public record TenantId(
        @Column(columnDefinition = "UUID")
        UUID tenantId
) {

    /**
     * Creates an unassigned tenant identifier.
     * <p>
     * Use this constructor when the user should not be linked to a tenant yet.
     */
    public TenantId() {
        this(null);
    }

    /**
     * Creates a tenant identifier with the provided UUID.
     *
     * @param tenantId the tenant UUID, or {@code null} to keep it unassigned
     */
    public TenantId {
    }

    /**
     * Returns whether this tenant identifier has been assigned.
     *
     * @return {@code true} when a tenant UUID is present; otherwise {@code false}
     */
    public boolean isAssigned() {
        return tenantId != null;
    }
}