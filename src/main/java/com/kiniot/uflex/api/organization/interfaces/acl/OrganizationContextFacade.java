package com.kiniot.uflex.api.organization.interfaces.acl;

import java.util.List;

public interface OrganizationContextFacade {
    boolean existsPatientById(String patientId);

    boolean existsPatientByIdAndClinicId(String patientId, String clinicId);

    List<String> findPatientIdsByPhysiotherapistIdAndClinicId(String physiotherapistId, String clinicId);
}
