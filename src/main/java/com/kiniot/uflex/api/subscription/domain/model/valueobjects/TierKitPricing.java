package com.kiniot.uflex.api.subscription.domain.model.valueobjects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.util.Objects;

@Embeddable
public record TierKitPricing(
        @Column(nullable = false)
        Integer baseKits,
        @Column(nullable = false)
        Boolean additionalKitsAllowed,
        Integer maxAdditionalKits,
        @Embedded
        @AttributeOverrides({
                @AttributeOverride(name = "amount", column = @Column(name = "pen_kit_unit_price_amount", nullable = false, precision = 15, scale = 2)),
                @AttributeOverride(name = "currency", column = @Column(name = "pen_kit_unit_price_currency", nullable = false))
        })
        Money penKitUnitPrice,
        @Embedded
        @AttributeOverrides({
                @AttributeOverride(name = "amount", column = @Column(name = "usd_kit_unit_price_amount", nullable = false, precision = 15, scale = 2)),
                @AttributeOverride(name = "currency", column = @Column(name = "usd_kit_unit_price_currency", nullable = false))
        })
        Money usdKitUnitPrice
) {
    public TierKitPricing {
        if (baseKits == null || baseKits < 0)
            throw new IllegalArgumentException("Base kits must be a non-negative integer");
        if (additionalKitsAllowed == null)
            throw new IllegalArgumentException("Additional kits allowed flag cannot be null");
        if (maxAdditionalKits != null && maxAdditionalKits < 0)
            throw new IllegalArgumentException("Max additional kits must be non-negative when provided");
        if (!additionalKitsAllowed && maxAdditionalKits != null && maxAdditionalKits != 0)
            throw new IllegalArgumentException("Max additional kits must be null or zero when additional kits are not allowed");
        if (additionalKitsAllowed && maxAdditionalKits != null && maxAdditionalKits == 0)
            throw new IllegalArgumentException("Max additional kits cannot be zero when additional kits are allowed");
        if (penKitUnitPrice == null)
            throw new IllegalArgumentException("PEN kit unit price cannot be null");
        if (usdKitUnitPrice == null)
            throw new IllegalArgumentException("USD kit unit price cannot be null");
        if (penKitUnitPrice.currency() != CurrencyCode.PEN)
            throw new IllegalArgumentException("PEN kit unit price must use PEN currency");
        if (usdKitUnitPrice.currency() != CurrencyCode.USD)
            throw new IllegalArgumentException("USD kit unit price must use USD currency");
    }

    public boolean hasUnlimitedAdditionalKits() {
        return additionalKitsAllowed && maxAdditionalKits == null;
    }

    public Money resolveUnitPrice(CurrencyCode currencyCode) {
        if (currencyCode == null)
            throw new IllegalArgumentException("Currency code cannot be null");
        return switch (currencyCode) {
            case PEN -> penKitUnitPrice;
            case USD -> usdKitUnitPrice;
        };
    }

    public int calculateAdditionalKits(int requestedTotalKits) {
        validateRequestedTotalKits(requestedTotalKits);
        return Math.max(0, requestedTotalKits - baseKits);
    }

    public Money calculateTotalCharge(int requestedTotalKits, CurrencyCode currencyCode) {
        validateRequestedTotalKits(requestedTotalKits);
        return resolveUnitPrice(currencyCode).multiply(requestedTotalKits);
    }

    public void validateRequestedTotalKits(int requestedTotalKits) {
        if (requestedTotalKits < baseKits)
            throw new IllegalArgumentException("Requested kits cannot be lower than the tier base kits");
        int additionalKits = requestedTotalKits - baseKits;
        if (additionalKits == 0) return;
        if (!additionalKitsAllowed)
            throw new IllegalArgumentException("This tier does not allow additional kits");
        if (maxAdditionalKits != null && additionalKits > maxAdditionalKits)
            throw new IllegalArgumentException("Requested additional kits exceed the tier limit");
    }
}
