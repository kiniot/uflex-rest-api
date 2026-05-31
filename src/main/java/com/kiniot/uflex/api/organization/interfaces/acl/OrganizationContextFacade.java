package com.kiniot.uflex.api.organization.interfaces.acl;

public interface OrganizationContextFacade {
    boolean existsPatientById(String patientId);

    boolean existsPatientByIdAndClinicId(String patientId, String clinicId);
}
