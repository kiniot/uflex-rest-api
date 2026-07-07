package com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.iam.interfaces.acl.IamContextFacade;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
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

    public Optional<UserId> fetchCurrentUserId() {
        var userId = iamContextFacade.fetchContextUserId();
        return (userId == null || userId.isBlank()) ? Optional.empty() : Optional.of(new UserId(UUID.fromString(userId)));
    }

    /**
     * The kit serial bound to the current principal when it is an edge service account;
     * empty for human callers. Used to enforce per-edge least-privilege on ingestion.
     */
    public Optional<String> findEdgeSerialForCurrentUser() {
        return iamContextFacade.findEdgeSerialForCurrentUser();
    }

    /**
     * The last LAN base URL reported by the edge bound to {@code serialNumber}; empty when no edge
     * account exists for it or it has not reported a URL yet. Used by the mobile rendezvous endpoint.
     */
    public Optional<String> findEdgeLanUrlBySerial(String serialNumber) {
        return iamContextFacade.findEdgeLanUrlBySerial(serialNumber);
    }
}
