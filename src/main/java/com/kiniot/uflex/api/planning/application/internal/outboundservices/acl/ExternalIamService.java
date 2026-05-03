package com.kiniot.uflex.api.planning.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.iam.interfaces.acl.IamContextFacade;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service("billingExternalIamService")
public class ExternalIamService {

    private final IamContextFacade iamContextFacade;

    public ExternalIamService(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    public Optional<ClinicId> fetchCurrentAcademyId() {
        var clinicId = iamContextFacade.fetchAuthenticatedUserTenantId();
        return clinicId.isEmpty() ? Optional.empty() : Optional.of(new ClinicId(UUID.fromString(clinicId)));
    }
}