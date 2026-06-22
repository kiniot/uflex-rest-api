package com.kiniot.uflex.api.iam.domain.model.commands;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.EdgeServiceAccountId;

/** Revokes an edge service account: removes the account and its ROLE_EDGE user. */
public record RevokeEdgeServiceAccountCommand(EdgeServiceAccountId edgeServiceAccountId) {
    public RevokeEdgeServiceAccountCommand {
        if (edgeServiceAccountId == null) {
            throw new IllegalArgumentException("EdgeServiceAccount ID cannot be null");
        }
    }
}
