package com.kiniot.uflex.api.iam.interfaces.rest.transform;

import com.kiniot.uflex.api.iam.domain.model.commands.ProvisionEdgeServiceAccountCommand;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.ProvisionEdgeServiceAccountResource;

public class ProvisionEdgeServiceAccountCommandFromResourceAssembler {

    public static ProvisionEdgeServiceAccountCommand toCommandFromResource(ProvisionEdgeServiceAccountResource resource) {
        return new ProvisionEdgeServiceAccountCommand(resource.serialNumber(), resource.clinicId());
    }
}
