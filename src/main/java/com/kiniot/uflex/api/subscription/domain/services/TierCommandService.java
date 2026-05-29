package com.kiniot.uflex.api.subscription.domain.services;

import com.kiniot.uflex.api.subscription.domain.model.commands.SeedTiersCommand;

public interface TierCommandService {
    void handle(SeedTiersCommand command);
}
