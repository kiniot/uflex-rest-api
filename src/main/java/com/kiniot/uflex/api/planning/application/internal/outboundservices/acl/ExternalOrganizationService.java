package com.kiniot.uflex.api.planning.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.organization.interfaces.acl.OrganizationContextFacade;
import com.kiniot.uflex.api.planning.domain.exceptions.PatientClinicMismatchException;
import com.kiniot.uflex.api.planning.domain.exceptions.PatientWithIdNotFoundException;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExternalOrganizationService {

    private final OrganizationContextFacade organizationContextFacade;

    public ExternalOrganizationService(OrganizationContextFacade organizationContextFacade) {
        this.organizationContextFacade = organizationContextFacade;
    }

    public void validatePatientBelongsToClinic(PatientId patientId, ClinicId clinicId) {
        var patientIdText = patientId.patientId().toString();
        var clinicIdText = clinicId.id().toString();

        if (!organizationContextFacade.existsPatientById(patientIdText)) {
            throw new PatientWithIdNotFoundException(patientIdText);
        }

        if (!organizationContextFacade.existsPatientByIdAndClinicId(patientIdText, clinicIdText)) {
            throw new PatientClinicMismatchException(patientIdText, clinicIdText);
        }
    }

    public List<PatientId> findPatientIdsByPhysiotherapistAndClinic(PhysiotherapistId physiotherapistId, ClinicId clinicId) {
        return organizationContextFacade.findPatientIdsByPhysiotherapistIdAndClinicId(
                        physiotherapistId.physiotherapistId().toString(),
                        clinicId.id().toString()
                ).stream()
                .map(java.util.UUID::fromString)
                .map(PatientId::new)
                .toList();
    }
}
