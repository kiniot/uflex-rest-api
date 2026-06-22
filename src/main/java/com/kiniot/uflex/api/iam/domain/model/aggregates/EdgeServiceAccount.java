package com.kiniot.uflex.api.iam.domain.model.aggregates;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.EdgeServiceAccountId;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.TenantId;
import com.kiniot.uflex.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * Aggregate root linking an edge service-account {@link User} (a {@code ROLE_EDGE}
 * principal) to the IoT kit it serves.
 * <p>
 * Each physical edge owns its own service account ({@code userId}) and is bound to a
 * single kit {@code serialNumber} within a {@code tenantId} (clinic). This binding is
 * what enables per-edge least-privilege: ingestion endpoints check that the calling
 * edge only writes for its own kit. It is kept as a dedicated aggregate so the human
 * {@link User} aggregate stays free of service-account-only concerns.
 */
@Getter
@Entity
@Table(indexes = {
        @Index(name = "idx_edge_service_account_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_edge_service_account_serial_number", columnList = "serial_number", unique = true)
})
public class EdgeServiceAccount extends AuditableAbstractAggregateRoot<EdgeServiceAccount, EdgeServiceAccountId> {

    @EmbeddedId
    private EdgeServiceAccountId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", columnDefinition = "UUID", nullable = false))
    private UserId userId;

    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @Embedded
    @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id", columnDefinition = "UUID", nullable = false))
    private TenantId tenantId;

    protected EdgeServiceAccount() {}

    public EdgeServiceAccount(UserId userId, String serialNumber, TenantId tenantId) {
        if (serialNumber == null || serialNumber.isBlank())
            throw new IllegalArgumentException("serialNumber cannot be null or blank");
        if (tenantId == null || !tenantId.isAssigned())
            throw new IllegalArgumentException("tenantId must be assigned");
        this.id = new EdgeServiceAccountId();
        this.userId = userId;
        this.serialNumber = serialNumber;
        this.tenantId = tenantId;
    }

    @Override
    public EdgeServiceAccountId getId() {
        return id;
    }
}
