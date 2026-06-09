package com.kiniot.uflex.api.device.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.organization.interfaces.acl.OrganizationContextFacade;
import org.springframework.stereotype.Service;

@Service("deviceExternalOrganizationService")
public class ExternalOrganizationService {

    private final OrganizationContextFacade organizationContextFacade;

    public ExternalOrganizationService(OrganizationContextFacade organizationContextFacade) {
        this.organizationContextFacade = organizationContextFacade;
    }

    public String getPatientFullName(String patientId) {
        return organizationContextFacade.getPatientFullName(patientId);
    }

    public boolean patientBelongsToClinic(String patientId, String clinicId) {
        return organizationContextFacade.existsPatientByIdAndClinicId(patientId, clinicId);
    }
}