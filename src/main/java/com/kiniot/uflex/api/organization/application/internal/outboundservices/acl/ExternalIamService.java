package com.kiniot.uflex.api.organization.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.RoleName;
import com.kiniot.uflex.api.iam.interfaces.acl.IamContextFacade;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExternalIamService {

    private final IamContextFacade iamContextFacade;

    public ExternalIamService(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    public Optional<ClinicId> fetchCurrentClinicId() {
        var clinicIdStr = iamContextFacade.fetchContextTenantId();
        if (clinicIdStr == null || clinicIdStr.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new ClinicId(UUID.fromString(clinicIdStr)));
    }

    public Optional<UserId> fetchCurrentUserId() {
        var userIdStr = iamContextFacade.fetchContextUserId();
        if (userIdStr == null || userIdStr.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new UserId(UUID.fromString(userIdStr)));
    }

    public Optional<String> fetchUserEmailAddressByUserId(String userId) {
        var email = iamContextFacade.fetchUserEmailAddressByUserId(userId);
        return email.isBlank() ? Optional.empty() : Optional.of(email);
    }

    public Optional<UserId> registerPhysiotherapist(String email) {
        var userId = iamContextFacade.signUpVerifiedUser(email, List.of(RoleName.ROLE_PHYSIOTHERAPIST.name()));
        return userId.isEmpty() ? Optional.empty() : Optional.of(new UserId(UUID.fromString(userId)));
    }

    public Optional<UserId> registerPatient(String email) {
        var userId = iamContextFacade.signUpVerifiedUser(email, List.of(RoleName.ROLE_PATIENT.name()));
        return userId.isEmpty() ? Optional.empty() : Optional.of(new UserId(UUID.fromString(userId)));
    }

    public void deleteUserById(UserId userId) {
        iamContextFacade.deleteUserById(userId.id().toString());
    }
}
