package com.kiniot.uflex.api.planning.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.organization.interfaces.acl.OrganizationContextFacade;
import com.kiniot.uflex.api.planning.domain.exceptions.PatientClinicMismatchException;
import com.kiniot.uflex.api.planning.domain.exceptions.PatientWithIdNotFoundException;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import org.springframework.stereotype.Service;

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
}
