package com.kiniot.uflex.api.planning.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.iam.interfaces.acl.IamContextFacade;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service("billingExternalIamService")
public class ExternalIamService {

    private final IamContextFacade iamContextFacade;

    public ExternalIamService(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    public Optional<ClinicId> fetchCurrentClinicId() {
        var clinicId = iamContextFacade.fetchContextTenantId();
        return clinicId.isEmpty() ? Optional.empty() : Optional.of(new ClinicId(UUID.fromString(clinicId)));
    }

    public Optional<UserId> fetchCurrentUserId() {
        var userId = iamContextFacade.fetchContextUserId();
        return userId.isEmpty() ? Optional.empty() : Optional.of(new UserId(UUID.fromString(userId)));
    }
}
