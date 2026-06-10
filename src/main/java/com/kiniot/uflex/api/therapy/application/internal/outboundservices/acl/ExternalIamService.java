package com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.iam.interfaces.acl.IamContextFacade;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service("therapyExternalIamService")
public class ExternalIamService {

    private final IamContextFacade iamContextFacade;

    public ExternalIamService(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    public Optional<ClinicId> fetchCurrentClinicId() {
        var clinicId = iamContextFacade.fetchCurrentTenantId();
        return clinicId.isEmpty() ? Optional.empty() : Optional.of(new ClinicId(UUID.fromString(clinicId)));
    }
}
