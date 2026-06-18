package com.kiniot.uflex.api.media.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.iam.interfaces.acl.IamContextFacade;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Anti-corruption layer between the media context and IAM. Resolves the
 * authenticated user's id and clinic (tenant) so media assets can be scoped and
 * audited without the media context depending on IAM internals.
 */
@Service("mediaExternalIamService")
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

    public Optional<UUID> fetchCurrentUserId() {
        var userIdStr = iamContextFacade.fetchContextUserId();
        if (userIdStr == null || userIdStr.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(userIdStr));
    }
}
