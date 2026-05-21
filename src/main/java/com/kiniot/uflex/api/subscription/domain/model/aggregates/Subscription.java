package com.kiniot.uflex.api.subscription.domain.model.aggregates;

import com.kiniot.uflex.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.subscription.domain.model.commands.CreateSubscriptionCommand;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionId;
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

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private LocalDate startedAt;

    private LocalDate renewsAt;

    private LocalDate endsAt;

    private LocalDate cancelledAt;

    protected Subscription() {}

    public Subscription(CreateSubscriptionCommand command, ClinicId clinicId) {
        this.id = new SubscriptionId();
        this.selection = command.selection();
        this.contractedPrice = command.contractedPrice();
        this.status = SubscriptionStatus.ACTIVE;
        this.startedAt = LocalDate.now();
        this.renewsAt = calculateRenewsAt(this.startedAt);
        this.endsAt = null;
        this.cancelledAt = null;
        this.clinicId = clinicId;
    }

    public void cancel() {
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

    private LocalDate calculateRenewsAt(LocalDate startedAt) {
        return switch (this.selection.billingPeriod()) {
            case MONTHLY -> startedAt.plusMonths(1);
            case YEARLY -> startedAt.plusYears(1);
        };
    }
}
