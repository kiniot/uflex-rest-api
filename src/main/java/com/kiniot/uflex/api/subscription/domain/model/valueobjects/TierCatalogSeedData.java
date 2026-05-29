package com.kiniot.uflex.api.subscription.domain.model.valueobjects;

import java.math.BigDecimal;
import java.util.List;

public final class TierCatalogSeedData {

    private TierCatalogSeedData() {}

    public static List<TierCatalog> catalogs() {
        return List.of(
                new TierCatalog(
                        TierName.TRIAL,
                        new TierLimits(1, 1, 1),
                        new TierKitPricing(
                                1,
                                false,
                                0,
                                money("0.00", CurrencyCode.PEN),
                                money("0.00", CurrencyCode.USD)
                        ),
                        false,
                        List.of(
                                new TierCatalogPrice(BillingPeriod.MONTHLY, money("0.00", CurrencyCode.PEN)),
                                new TierCatalogPrice(BillingPeriod.MONTHLY, money("0.00", CurrencyCode.USD)),
                                new TierCatalogPrice(BillingPeriod.YEARLY, money("0.00", CurrencyCode.PEN)),
                                new TierCatalogPrice(BillingPeriod.YEARLY, money("0.00", CurrencyCode.USD))
                        )
                ),
                new TierCatalog(
                        TierName.PILOT,
                        new TierLimits(2, 15, 2),
                        new TierKitPricing(
                                1,
                                true,
                                1,
                                money("350.00", CurrencyCode.PEN),
                                money("99.00", CurrencyCode.USD)
                        ),
                        false,
                        List.of(
                                new TierCatalogPrice(BillingPeriod.MONTHLY, money("290.00", CurrencyCode.PEN)),
                                new TierCatalogPrice(BillingPeriod.MONTHLY, money("90.00", CurrencyCode.USD)),
                                new TierCatalogPrice(BillingPeriod.YEARLY, money("2900.00", CurrencyCode.PEN)),
                                new TierCatalogPrice(BillingPeriod.YEARLY, money("900.00", CurrencyCode.USD))
                        )
                ),
                new TierCatalog(
                        TierName.PROFESSIONAL,
                        new TierLimits(null, 50, 8),
                        new TierKitPricing(
                                5,
                                true,
                                null,
                                money("300.00", CurrencyCode.PEN),
                                money("89.00", CurrencyCode.USD)
                        ),
                        false,
                        List.of(
                                new TierCatalogPrice(BillingPeriod.MONTHLY, money("790.00", CurrencyCode.PEN)),
                                new TierCatalogPrice(BillingPeriod.MONTHLY, money("230.00", CurrencyCode.USD)),
                                new TierCatalogPrice(BillingPeriod.YEARLY, money("7900.00", CurrencyCode.PEN)),
                                new TierCatalogPrice(BillingPeriod.YEARLY, money("2300.00", CurrencyCode.USD))
                        )
                ),
                new TierCatalog(
                        TierName.ENTERPRISE,
                        new TierLimits(null, null, null),
                        new TierKitPricing(
                                50,
                                true,
                                null,
                                money("280.00", CurrencyCode.PEN),
                                money("79.00", CurrencyCode.USD)
                        ),
                        true,
                        List.of(
                                new TierCatalogPrice(BillingPeriod.MONTHLY, money("1990.00", CurrencyCode.PEN)),
                                new TierCatalogPrice(BillingPeriod.MONTHLY, money("580.00", CurrencyCode.USD)),
                                new TierCatalogPrice(BillingPeriod.YEARLY, money("19900.00", CurrencyCode.PEN)),
                                new TierCatalogPrice(BillingPeriod.YEARLY, money("5800.00", CurrencyCode.USD))
                        )
                )
        );
    }

    private static Money money(String amount, CurrencyCode currency) {
        return new Money(new BigDecimal(amount), currency);
    }
}
