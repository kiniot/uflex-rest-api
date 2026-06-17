package com.kiniot.uflex.api.device.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.iam.interfaces.acl.IamContextFacade;
import com.kiniot.uflex.api.organization.interfaces.acl.OrganizationContextFacade;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service("deviceExternalIamService")
public class ExternalIamService {

    private final IamContextFacade iamContextFacade;
    private final OrganizationContextFacade organizationContextFacade;

    public ExternalIamService(
            IamContextFacade iamContextFacade,
            OrganizationContextFacade organizationContextFacade
    ) {
        this.iamContextFacade = iamContextFacade;
        this.organizationContextFacade = organizationContextFacade;
    }

    public Optional<ClinicId> fetchCurrentClinicId() {
        var clinicIdStr = iamContextFacade.fetchContextTenantId();
        if (clinicIdStr == null || clinicIdStr.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new ClinicId(UUID.fromString(clinicIdStr)));
    }

    public Optional<PatientId> getCurrentPatientId() {
        var userId = iamContextFacade.fetchContextUserId();
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        var patientIdStr = organizationContextFacade.findPatientIdByUserId(userId);
        if (patientIdStr == null || patientIdStr.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new PatientId(UUID.fromString(patientIdStr)));
    }
}