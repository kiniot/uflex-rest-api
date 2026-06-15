package com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.organization.interfaces.acl.OrganizationContextFacade;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.PatientId;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service("therapyExternalOrganizationService")
public class ExternalOrganizationService {

    private final OrganizationContextFacade organizationContextFacade;
    private final ExternalIamService externalIamService;

    public ExternalOrganizationService(
            OrganizationContextFacade organizationContextFacade,
            ExternalIamService externalIamService
    ) {
        this.organizationContextFacade = organizationContextFacade;
        this.externalIamService = externalIamService;
    }

    public boolean patientBelongsToClinic(String patientId, String clinicId) {
        return organizationContextFacade.existsPatientByIdAndClinicId(patientId, clinicId);
    }

    public Optional<PatientId> fetchCurrentPatientId() {
        var userId = externalIamService.fetchCurrentUserId();
        if (userId.isEmpty()) {
            return Optional.empty();
        }
        var patientId = organizationContextFacade.findPatientIdByUserId(userId.get().id().toString());
        if (patientId == null || patientId.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(PatientId.of(UUID.fromString(patientId)));
    }
}
