package com.kiniot.uflex.api.subscription.domain.model.entities;

import com.kiniot.uflex.api.shared.domain.model.entities.AuditableModel;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.TierLimits;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.TierName;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.TierId;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Tier extends AuditableModel<TierId> {

    @EmbeddedId
    private TierId id;

    @Enumerated(EnumType.STRING)
    private TierName name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tier_id", nullable = false)
    List<TierPrice> prices;

    @Embedded
    private TierLimits tierLimits;

    protected Tier() {}

    public Tier(TierName name) {
        this.id = new TierId();
        this.name = name;
        this.prices = new ArrayList<>();
    }

}
