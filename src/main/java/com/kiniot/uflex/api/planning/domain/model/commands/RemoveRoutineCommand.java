package com.kiniot.uflex.api.planning.domain.model.commands;

import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineOrder;

public record RemoveRoutineCommand(
        RoutineOrder order
) {
    public RemoveRoutineCommand {
        if (order == null) {
            throw new IllegalArgumentException("Routine order cannot be null");
        }
    }
}
