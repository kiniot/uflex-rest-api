package com.kiniot.uflex.api.iam.domain.services;

import com.kiniot.uflex.api.iam.domain.model.commands.ProvisionEdgeServiceAccountCommand;
import com.kiniot.uflex.api.iam.domain.model.commands.ReportEdgeLanUrlCommand;
import com.kiniot.uflex.api.iam.domain.model.commands.RevokeEdgeServiceAccountCommand;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.EdgeServiceAccountCredentials;

public interface EdgeServiceAccountCommandService {

    /**
     * Provisions a new edge service account and returns its plaintext credential once.
     *
     * @param command the provisioning command (kit serial)
     * @return the freshly generated credential (email + password)
     */
    EdgeServiceAccountCredentials handle(ProvisionEdgeServiceAccountCommand command);

    /** Revokes an edge service account, deleting it and its ROLE_EDGE user. */
    void handle(RevokeEdgeServiceAccountCommand command);

    /**
     * Records the LAN base URL reported by the currently authenticated edge. The target
     * account is resolved from the authenticated principal, so an edge can only update its own.
     */
    void handle(ReportEdgeLanUrlCommand command);
}
