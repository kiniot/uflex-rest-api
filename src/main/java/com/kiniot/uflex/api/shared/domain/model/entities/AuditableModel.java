package com.kiniot.uflex.api.shared.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

/**
 * Base class for all entities that require auditing.
 *
 * @summary The class is a base entity class that provides auditing fields such as createdAt and updatedAt.
 * @since 1.0.0
 */
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class AuditableModel {

    @Getter
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @Getter
    @LastModifiedDate
    @Column(nullable = false)
    private Date updatedAt;
}