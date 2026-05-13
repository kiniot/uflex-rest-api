package com.kiniot.uflex.api.subscription.domain.model.aggregates;

import com.kiniot.uflex.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.subscription.domain.model.entities.Invoice;
import com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.PaymentReference;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionStatus;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(indexes = {
        @Index(name = "idx_subscription_clinic_id", columnList = "clinic_id"),
        @Index(name = "idx_subscription_status", columnList = "status")
})
public class Subscription extends AuditableAbstractAggregateRoot<Subscription, SubscriptionId> {

    @EmbeddedId
    private SubscriptionId id;

    @Embedded
    @jakarta.persistence.AttributeOverride(name = "id", column = @jakarta.persistence.Column(name = "clinic_id", columnDefinition = "UUID", nullable = false))
    private ClinicId clinicId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    private OffsetDateTime currentPeriodStart;

    private OffsetDateTime currentPeriodEnd;

    private OffsetDateTime nextBillingDate;

    private OffsetDateTime trialUntil;

    @Embedded
    private PaymentReference paymentReference;

    @Transient
    private List<Invoice> invoices;

    protected Subscription() {
        this.invoices = new ArrayList<>();
    }

    public Subscription(ClinicId clinicId, SubscriptionPlan plan, BillingCycle billingCycle, PaymentReference paymentReference) {
        this.id = new SubscriptionId();
        this.clinicId = clinicId;
        this.plan = plan;
        this.billingCycle = billingCycle;
        this.paymentReference = paymentReference;
        this.invoices = new ArrayList<>();
        this.status = SubscriptionStatus.PENDING_PAYMENT;
    }

    public void activate(OffsetDateTime activatedAt) {
        if (paymentReference == null || paymentReference.providerTransactionId() == null) {
            throw new IllegalStateException("Subscription cannot be activated without confirmed payment");
        }
        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodStart = activatedAt;
        this.currentPeriodEnd = billingCycle == BillingCycle.YEARLY ? activatedAt.plusYears(1) : activatedAt.plusMonths(1);
        this.nextBillingDate = this.currentPeriodEnd;
    }

    public void changePlan(SubscriptionPlan newPlan, BillingCycle newBillingCycle, PaymentReference paymentReference, OffsetDateTime changedAt) {
        if (this.status != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Only active subscriptions can change plans");
        }
        if (paymentReference == null || paymentReference.providerTransactionId() == null) {
            throw new IllegalStateException("Plan change requires confirmed payment");
        }
        this.plan = newPlan;
        this.billingCycle = newBillingCycle;
        this.paymentReference = paymentReference;
        this.currentPeriodStart = changedAt;
        this.currentPeriodEnd = newBillingCycle == BillingCycle.YEARLY ? changedAt.plusYears(1) : changedAt.plusMonths(1);
        this.nextBillingDate = this.currentPeriodEnd;
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
    }

    public void markPastDue() {
        this.status = SubscriptionStatus.PAST_DUE;
    }

    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
    }

    public void updatePaymentReference(PaymentReference paymentReference) {
        this.paymentReference = paymentReference;
    }

    public void addInvoice(Invoice invoice) {
        this.invoices.add(invoice);
    }

    @Override
    public SubscriptionId getId() {
        return id;
    }
}
