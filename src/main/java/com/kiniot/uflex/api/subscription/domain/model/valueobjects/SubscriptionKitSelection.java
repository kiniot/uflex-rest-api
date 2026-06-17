package com.kiniot.uflex.api.subscription.domain.model.valueobjects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public record SubscriptionKitSelection(
        @Column(nullable = false)
        Integer requestedTotalKits,
        @Column(nullable = false)
        Integer baseKits,
        @Column(nullable = false)
        Integer additionalKits,
        @Embedded
        @AttributeOverrides({
                @AttributeOverride(name = "amount", column = @Column(name = "kit_unit_price_amount", nullable = false, precision = 15, scale = 2)),
                @AttributeOverride(name = "currency", column = @Column(name = "kit_unit_price_currency", nullable = false))
        })
        Money kitUnitPrice,
        @Embedded
        @AttributeOverrides({
                @AttributeOverride(name = "amount", column = @Column(name = "total_kit_charge_amount", nullable = false, precision = 15, scale = 2)),
                @AttributeOverride(name = "currency", column = @Column(name = "total_kit_charge_currency", nullable = false))
        })
        Money totalKitCharge
) {
    public SubscriptionKitSelection {
        if (requestedTotalKits == null || requestedTotalKits < 0)
            throw new IllegalArgumentException("Requested total kits must be a non-negative integer");
        if (baseKits == null || baseKits < 0)
            throw new IllegalArgumentException("Base kits must be a non-negative integer");
        if (additionalKits == null || additionalKits < 0)
            throw new IllegalArgumentException("Additional kits must be a non-negative integer");
        if (requestedTotalKits != baseKits + additionalKits)
            throw new IllegalArgumentException("Requested total kits must equal base kits plus additional kits");
        if (kitUnitPrice == null)
            throw new IllegalArgumentException("Kit unit price cannot be null");
        if (totalKitCharge == null)
            throw new IllegalArgumentException("Total kit charge cannot be null");
        if (kitUnitPrice.currency() != totalKitCharge.currency())
            throw new IllegalArgumentException("Kit unit price and total kit charge must share the same currency");
    }
}
