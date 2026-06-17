package com.kiniot.uflex.api.subscription.domain.model.commands;

import com.kiniot.uflex.api.subscription.domain.exceptions.InvalidSubscriptionAmountFormatException;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingPeriod;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.CurrencyCode;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionSelection;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.TierId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateSubscriptionCommandTests {

    @Test
    void shouldRejectAmountWithoutExactlyTwoFractionalDigits() {
        var selection = new SubscriptionSelection(new TierId(UUID.randomUUID()), BillingPeriod.MONTHLY);
        var contractedPrice = new Money(new BigDecimal("90"), CurrencyCode.USD);

        assertThrows(InvalidSubscriptionAmountFormatException.class,
                () -> new CreateSubscriptionCommand(selection, contractedPrice, 1));
    }

    @Test
    void shouldAllowAmountWithExactlyTwoFractionalDigits() {
        var selection = new SubscriptionSelection(new TierId(UUID.randomUUID()), BillingPeriod.MONTHLY);
        var contractedPrice = new Money(new BigDecimal("90.00"), CurrencyCode.USD);

        assertDoesNotThrow(() -> new CreateSubscriptionCommand(selection, contractedPrice, 1));
    }
}
