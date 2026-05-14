package com.kiniot.uflex.api.subscription.domain.model.entities;

import com.kiniot.uflex.api.shared.domain.model.entities.AuditableModel;
import com.kiniot.uflex.api.subscription.domain.model.commands.CreatePlanCommand;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionPlanId;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(indexes = {
        @Index(name = "idx_subscription_plan_code", columnList = "code", unique = true),
        @Index(name = "idx_subscription_plan_active", columnList = "active")
})
public class SubscriptionPlan extends AuditableModel<SubscriptionPlanId> {

    @EmbeddedId
    private SubscriptionPlanId id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 40, unique = true)
    private String code;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "monthly_price", nullable = false, precision = 12, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "monthly_price_currency", nullable = false, length = 3))
    })
    private Money monthlyPrice;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "yearly_price", nullable = false, precision = 12, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "yearly_price_currency", nullable = false, length = 3))
    })
    private Money yearlyPrice;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private Integer maxPatients;

    @Column(nullable = false)
    private Integer maxPhysiotherapists;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "subscription_plan_features", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "feature", nullable = false, length = 160)
    private List<String> features;

    @Column(nullable = false)
    private boolean active;

    protected SubscriptionPlan() {
    }

    public SubscriptionPlan(CreatePlanCommand command) {
        this.id = new SubscriptionPlanId();
        this.name = command.name();
        this.code = command.code();
        this.monthlyPrice = command.monthlyPrice();
        this.yearlyPrice = command.yearlyPrice();
        this.currency = command.monthlyPrice().currency();
        this.maxPatients = command.maxPatients();
        this.maxPhysiotherapists = command.maxPhysiotherapists();
        this.features = new ArrayList<>(command.features());
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public Money priceFor(BillingCycle billingCycle) {
        return billingCycle == BillingCycle.YEARLY ? yearlyPrice : monthlyPrice;
    }

    public Money amountForPlanChange(BillingCycle billingCycle) {
        // TODO: replace this simple full-cycle charge with prorated billing when billing rules are defined.
        return priceFor(billingCycle);
    }

    @Override
    public SubscriptionPlanId getId() {
        return id;
    }
}
