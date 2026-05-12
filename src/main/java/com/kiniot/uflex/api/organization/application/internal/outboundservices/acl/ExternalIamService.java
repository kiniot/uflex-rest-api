package com.kiniot.uflex.api.organization.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.UserId;
import com.kiniot.uflex.api.iam.interfaces.acl.IamContextFacade;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ClinicId;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ExternalIamService {

    private final IamContextFacade iamContextFacade;

    public ExternalIamService(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    public Optional<ClinicId> fetchCurrentClinicId() {
        var clinicIdStr = iamContextFacade.fetchAuthenticatedUserTenantId();
        if (clinicIdStr == null || clinicIdStr.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new ClinicId(UUID.fromString(clinicIdStr)));
    }

    public Optional<UserId> fetchCurrentUserId() {
        var userIdStr = iamContextFacade.fetchAuthenticatedUserId();
        if (userIdStr == null || userIdStr.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new UserId(UUID.fromString(userIdStr)));
    }

    public Optional<String> fetchUserEmailAddressByUserId(String userId) {
        var email = iamContextFacade.fetchUserEmailAddressByUserId(userId);
        return email.isBlank() ? Optional.empty() : Optional.of(email);
    }
}