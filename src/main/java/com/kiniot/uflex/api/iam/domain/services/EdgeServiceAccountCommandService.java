package com.kiniot.uflex.api.iam.domain.services;

import com.kiniot.uflex.api.iam.domain.model.commands.ProvisionEdgeServiceAccountCommand;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.EdgeServiceAccountCredentials;

public interface EdgeServiceAccountCommandService {

    /**
     * Provisions a new edge service account and returns its plaintext credential once.
     *
     * @param command the provisioning command (kit serial)
     * @return the freshly generated credential (email + password)
     */
    EdgeServiceAccountCredentials handle(ProvisionEdgeServiceAccountCommand command);
}
