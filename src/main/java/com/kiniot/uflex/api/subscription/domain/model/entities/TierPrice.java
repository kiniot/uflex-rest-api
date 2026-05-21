package com.kiniot.uflex.api.subscription.domain.model.entities;

import com.kiniot.uflex.api.shared.domain.model.entities.AuditableModel;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingPeriod;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.TierPriceId;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class TierPrice extends AuditableModel<TierPriceId> {

    @EmbeddedId
    private TierPriceId id;

    @Enumerated(EnumType.STRING)
    private BillingPeriod billingPeriod;

    @Embedded
    private Money price;

    protected TierPrice() {}

    public TierPrice(BillingPeriod billingPeriod, Money price) {
        this.id = new TierPriceId();
        this.billingPeriod = billingPeriod;
        this.price = price;
    }
}
