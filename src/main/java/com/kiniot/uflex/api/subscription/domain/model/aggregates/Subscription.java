package com.kiniot.uflex.api.subscription.domain.model.aggregates;

import com.kiniot.uflex.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.subscription.domain.model.commands.CreateSubscriptionCommand;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionKitSelection;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionSelection;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

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
    @AttributeOverride(name = "id", column = @Column(name = "clinic_id", columnDefinition = "UUID"))
    private ClinicId clinicId;

    @Embedded
    @AttributeOverride(name = "tierId.id", column = @Column(name = "tier_id", columnDefinition = "UUID", nullable = false, unique = false))
    private SubscriptionSelection selection;

    @Embedded
    private Money contractedPrice;

    @Embedded
    private SubscriptionKitSelection kitSelection;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private String checkoutSessionId;

    private LocalDate startedAt;

    private LocalDate renewsAt;

    private LocalDate endsAt;

    private LocalDate cancelledAt;

    protected Subscription() {}

    public Subscription(CreateSubscriptionCommand command, ClinicId clinicId, SubscriptionKitSelection kitSelection) {
        this.id = new SubscriptionId();
        this.selection = command.selection();
        this.contractedPrice = command.contractedPrice();
        this.kitSelection = kitSelection;
        this.status = SubscriptionStatus.PENDING;
        this.checkoutSessionId = null;
        this.startedAt = null;
        this.renewsAt = null;
        this.endsAt = null;
        this.cancelledAt = null;
        this.clinicId = clinicId;
    }

    public void attachCheckoutSession(String checkoutSessionId) {
        if (checkoutSessionId == null || checkoutSessionId.isBlank())
            throw new IllegalArgumentException("Checkout session ID cannot be null or empty");
        if (this.status != SubscriptionStatus.PENDING)
            throw new IllegalStateException("Checkout session can only be attached to a pending subscription");
        this.checkoutSessionId = checkoutSessionId;
    }

    public void activate() {
        if (this.status != SubscriptionStatus.PENDING && this.status != SubscriptionStatus.PAST_DUE)
            throw new IllegalStateException("Only pending or past due subscriptions can be activated");
        this.status = SubscriptionStatus.ACTIVE;
        this.startedAt = LocalDate.now();
        this.renewsAt = calculateRenewsAt(this.startedAt);
        this.endsAt = null;
        this.cancelledAt = null;
    }

    public void markPastDue() {
        if (this.status != SubscriptionStatus.ACTIVE)
            throw new IllegalStateException("Only active subscriptions can be marked as past due");
        this.status = SubscriptionStatus.PAST_DUE;
    }

    public void expirePendingCheckout() {
        if (this.status != SubscriptionStatus.PENDING)
            throw new IllegalStateException("Only pending subscriptions can expire checkout");
        this.endsAt = LocalDate.now();
        this.status = SubscriptionStatus.EXPIRED;
    }

    public void cancel() {
        if (this.status == SubscriptionStatus.PENDING)
            throw new IllegalStateException("Pending subscription cannot be canceled before payment confirmation");
        if (this.status == SubscriptionStatus.CANCELED)
            throw new IllegalStateException("Subscription is already canceled");
        if (this.status == SubscriptionStatus.EXPIRED)
            throw new IllegalStateException("Expired subscription cannot be canceled");
        this.cancelledAt = LocalDate.now();
        this.endsAt = this.renewsAt;
        this.status = SubscriptionStatus.CANCELED;
    }

    public void markExpired() {
        if (this.status == SubscriptionStatus.CANCELED)
            throw new IllegalStateException("Canceled subscription cannot be marked as expired");
        if (this.status == SubscriptionStatus.EXPIRED)
            throw new IllegalStateException("Subscription is already expired");
        this.endsAt = LocalDate.now();
        this.status = SubscriptionStatus.EXPIRED;
    }

    public boolean isCurrentAt(LocalDate date) {
        if (date == null)
            throw new IllegalArgumentException("Date cannot be null");
        if (this.status == SubscriptionStatus.ACTIVE || this.status == SubscriptionStatus.PAST_DUE)
            return true;
        return this.status == SubscriptionStatus.CANCELED
                && this.endsAt != null
                && !this.endsAt.isBefore(date);
    }

    public boolean blocksNewSubscriptionAt(LocalDate date) {
        if (date == null)
            throw new IllegalArgumentException("Date cannot be null");
        return this.status == SubscriptionStatus.PENDING || this.isCurrentAt(date);
    }

    private LocalDate calculateRenewsAt(LocalDate startedAt) {
        return switch (this.selection.billingPeriod()) {
            case MONTHLY -> startedAt.plusMonths(1);
            case YEARLY -> startedAt.plusYears(1);
        };
    }
}
