package com.kiniot.uflex.api.shared.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

/**
 * Base abstract class for entities incorporating auditing and optimized persistence state management.
 * <p>
 * This class implements {@link Persistable} to provide explicit control over whether an entity is
 * new or existing, optimizing database operations by avoiding unnecessary SELECT queries before
 * insertion. It also tracks creation and modification timestamps automatically.
 *
 * @param <ID> The type of the entity's identifier.
 */
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class AuditableModel<ID> implements Persistable<ID> {

    /**
     * The date and time when the entity was first persisted.
     * Managed automatically by {@link AuditingEntityListener}.
     */
    @Getter
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    /**
     * The date and time when the entity was last updated.
     * Managed automatically by {@link AuditingEntityListener}.
     */
    @Getter
    @LastModifiedDate
    @Column(nullable = false)
    private Date updatedAt;

    /**
     * Internal flag to track the persistence state of the entity.
     * Marked as {@link Transient} to prevent it from being persisted in the database.
     */
    @Transient
    protected boolean isPersisted = false;

    /**
     * Updates the {@link #isPersisted} flag to {@code true}.
     * <p>Triggered by JPA lifecycle events:</p>
     * <ul>
     *     <li>{@link PostLoad}: Triggered after an entity is loaded from the database.</li>
     *     <li>{@link PostPersist}: Triggered after a new entity is successfully saved.</li>
     * </ul>
     */
    @PostLoad
    @PostPersist
    protected void markPersisted() {
        this.isPersisted = true;
    }

    /**
     * Indicates whether the entity is new or already exists in the database.
     * <p>
     * Spring Data JPA uses this to decide between {@code SQL INSERT} and {@code SQL UPDATE}.
     *
     * @return {@code true} if the entity is new; {@code false} if it has been persisted.
     */
    @Override
    public boolean isNew() {
        return !isPersisted;
    }

    /**
     * Returns the unique identifier of the entity.
     * Must be implemented by concrete classes to return the field annotated with {@link Id} or {@link EmbeddedId}.
     *
     * @return The entity identifier of type {@code ID}.
     */
    @Override
    public abstract ID getId();
}