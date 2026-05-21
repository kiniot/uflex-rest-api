package com.kiniot.uflex.api.subscription.domain.model.entities;

import com.kiniot.uflex.api.shared.domain.model.entities.AuditableModel;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingPeriod;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.CurrencyCode;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.TierKitPricing;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.TierLimits;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.TierName;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.TierId;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
public class Tier extends AuditableModel<TierId> {

    @EmbeddedId
    private TierId id;

    @Enumerated(EnumType.STRING)
    private TierName name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tier_id", nullable = false)
    private List<TierPrice> prices;

    @Embedded
    private TierKitPricing tierKitPricing;

    @Embedded
    private TierLimits tierLimits;

    @Column(nullable = false)
    private boolean allowsPriceOverride;

    protected Tier() {}

    private Tier(
            TierName name,
            TierLimits tierLimits,
            TierKitPricing tierKitPricing,
            boolean allowsPriceOverride
    ) {
        this.id = new TierId();
        this.name = Objects.requireNonNull(name, "Tier name cannot be null");
        this.tierLimits = Objects.requireNonNull(tierLimits, "Tier limits cannot be null");
        this.tierKitPricing = Objects.requireNonNull(tierKitPricing, "Tier kit pricing cannot be null");
        this.allowsPriceOverride = allowsPriceOverride;
        this.prices = new ArrayList<>();
    }

    public static Tier create(
            TierName name,
            TierLimits tierLimits,
            TierKitPricing tierKitPricing,
            boolean allowsPriceOverride,
            List<TierPrice> prices
    ) {
        Objects.requireNonNull(prices, "Tier prices cannot be null");
        var tier = new Tier(name, tierLimits, tierKitPricing, allowsPriceOverride);
        prices.forEach(price -> {
            Objects.requireNonNull(price, "Tier price cannot be null");
            tier.addPrice(price.getBillingPeriod(), price.getPrice());
        });
        return tier;
    }

    public void updateLimits(TierLimits tierLimits) {
        this.tierLimits = Objects.requireNonNull(tierLimits, "Tier limits cannot be null");
    }

    public void updateKitPricing(TierKitPricing tierKitPricing) {
        this.tierKitPricing = Objects.requireNonNull(tierKitPricing, "Tier kit pricing cannot be null");
    }

    public void updatePriceOverridePolicy(boolean allowsPriceOverride) {
        this.allowsPriceOverride = allowsPriceOverride;
    }

    public boolean hasPrice(BillingPeriod billingPeriod, CurrencyCode currency) {
        Objects.requireNonNull(billingPeriod, "Billing period cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        return this.prices.stream()
                .anyMatch(tierPrice -> tierPrice.getBillingPeriod() == billingPeriod
                        && tierPrice.getPrice().currency() == currency);
    }

    public void addPrice(BillingPeriod billingPeriod, Money price) {
        Objects.requireNonNull(billingPeriod, "Billing period cannot be null");
        Objects.requireNonNull(price, "Price cannot be null");
        if (hasPrice(billingPeriod, price.currency())) {
            throw new IllegalArgumentException(
                    "Tier already has a price for period %s and currency %s"
                            .formatted(billingPeriod, price.currency()));
        }
        this.prices.add(new TierPrice(billingPeriod, price));
    }
}
