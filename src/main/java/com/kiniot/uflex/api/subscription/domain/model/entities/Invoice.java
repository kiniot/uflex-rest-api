package com.kiniot.uflex.api.subscription.domain.model.entities;

import com.kiniot.uflex.api.shared.domain.model.entities.AuditableModel;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.InvoiceId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.InvoiceStatus;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(indexes = {
        @Index(name = "idx_invoice_subscription_id", columnList = "subscription_id")
})
public class Invoice extends AuditableModel<InvoiceId> {

    @EmbeddedId
    private InvoiceId id;

    @Column(nullable = false, columnDefinition = "UUID")
    private UUID subscriptionId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false, precision = 12, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "amount_currency", nullable = false, length = 3))
    })
    private Money amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private OffsetDateTime issuedAt;

    @Column(nullable = false)
    private OffsetDateTime dueAt;

    private OffsetDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(length = 120)
    private String providerTransactionId;

    protected Invoice() {
    }

    public Invoice(UUID subscriptionId, Money amount, OffsetDateTime issuedAt, OffsetDateTime dueAt) {
        this.id = new InvoiceId();
        this.subscriptionId = subscriptionId;
        this.amount = amount;
        this.currency = amount.currency();
        this.issuedAt = issuedAt;
        this.dueAt = dueAt;
        this.status = InvoiceStatus.PENDING;
    }

    public void markPaid(String providerTransactionId, OffsetDateTime paidAt) {
        this.providerTransactionId = providerTransactionId;
        this.paidAt = paidAt;
        this.status = InvoiceStatus.PAID;
    }

    public void markFailed(String providerTransactionId) {
        this.providerTransactionId = providerTransactionId;
        this.status = InvoiceStatus.FAILED;
    }

    @Override
    public InvoiceId getId() {
        return id;
    }
}
