package com.kiniot.uflex.api.organization.interfaces.acl;

import java.util.List;
import java.util.Map;

public interface OrganizationContextFacade {
    boolean existsPatientById(String patientId);

    boolean existsPatientByIdAndClinicId(String patientId, String clinicId);

    List<String> findPatientIdsByPhysiotherapistIdAndClinicId(String physiotherapistId, String clinicId);

    String findPatientIdByUserId(String userId);

    /** Empty string when the user has no physiotherapist profile, mirroring findPatientIdByUserId. */
    String findPhysiotherapistIdByUserId(String userId);

    String getPatientFullName(String patientId);

    /** Maps patient ids to their full name. Unknown ids are omitted from the result. */
    Map<String, String> getPatientNamesByIds(List<String> patientIds);

    /** Maps clinic ids to their commercial name. Unknown ids are omitted from the result. */
    Map<String, String> getClinicNamesByIds(List<String> clinicIds);
}
