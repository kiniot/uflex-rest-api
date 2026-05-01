package com.kiniot.uflex.api.shared.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

/**
 * Base abstract class for Aggregate Roots incorporating auditing, domain event publishing,
 * and optimized persistence state management.
 * <p>
 * This class extends {@link AbstractAggregateRoot} to allow registration of domain events
 * and implements {@link Persistable} to provide explicit control over whether an entity is
 * new or existing. This avoids unnecessary database lookups before insertion.
 *
 * @param <T>  The specific aggregate root type (self-referencing).
 * @param <ID> The type of the aggregate's identifier.
 */
@Getter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class AuditableAbstractAggregateRoot<T extends AbstractAggregateRoot<T>, ID> extends AbstractAggregateRoot<T>
        implements Persistable<ID> {

    /**
     * The date and time when the entity was first persisted.
     * Managed automatically by {@link AuditingEntityListener}.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    /**
     * The date and time when the entity was last updated.
     * Managed automatically by {@link AuditingEntityListener}.
     */
    @LastModifiedDate
    @Column(nullable = false)
    private Date updatedAt;

    /**
     * Flag that indicates if the entity has been persisted to the database.
     * This field is marked as {@link Transient} to ensure it is not mapped to a
     * database column, serving only as an in-memory state indicator for the
     * persistence lifecycle.
     */
    @Transient
    protected boolean isPersisted = false;

    /**
     * Updates the {@code isPersisted} flag to true.
     * This method is automatically triggered by JPA lifecycle events:
     * <ul>
     *     <li>{@link PostLoad}: After an entity is retrieved from the database.</li>
     *     <li>{@link PostPersist}: After a new entity is successfully saved.</li>
     * </ul>
     * This ensures the entity is correctly identified as "not new" for future
     * persistence operations.
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
     * Returns the unique identifier of the aggregate.
     * Must be implemented by concrete classes to return the field annotated with {@link Id} or {@link EmbeddedId}.
     *
     * @return The entity identifier of type {@code ID}.
     */
    @Override
    public abstract ID getId();

    /**
     * Registers a domain event to be published when the aggregate is saved via a Spring Data Repository.
     *
     * @param event The domain event object to be registered.
     */
    public void addDomainEvent(Object event) {
        super.registerEvent(event);
    }
}