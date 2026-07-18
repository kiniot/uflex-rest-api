package com.kiniot.uflex.api.therapy.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.organization.interfaces.acl.OrganizationContextFacade;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.PatientId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

    /** The physiotherapist profile of the authenticated user, if they have one. */
    public Optional<String> fetchCurrentPhysiotherapistId() {
        var userId = externalIamService.fetchCurrentUserId();
        if (userId.isEmpty()) {
            return Optional.empty();
        }
        var physiotherapistId = organizationContextFacade
                .findPhysiotherapistIdByUserId(userId.get().id().toString());
        if (physiotherapistId == null || physiotherapistId.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(physiotherapistId);
    }

    /** The patients assigned to a physiotherapist within a clinic. */
    public List<PatientId> findPatientIdsByPhysiotherapistAndClinic(String physiotherapistId, String clinicId) {
        return organizationContextFacade
                .findPatientIdsByPhysiotherapistIdAndClinicId(physiotherapistId, clinicId)
                .stream()
                .map(UUID::fromString)
                .map(PatientId::of)
                .toList();
    }

    public Map<String, String> fetchPatientNames(List<PatientId> patientIds) {
        return organizationContextFacade.getPatientNamesByIds(
                patientIds.stream().map(patientId -> patientId.id().toString()).toList());
    }
}
