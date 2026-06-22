package com.kiniot.uflex.api.iam.interfaces.rest.resources;

/**
 * Request body to provision an edge service account.
 *
 * @param serialNumber the kit serial the edge serves
 * @param clinicId     owning clinic UUID; required for ROLE_DEVELOPER callers, optional for
 *                     ROLE_CLINIC_ADMIN (inferred from their own clinic when omitted)
 */
public record ProvisionEdgeServiceAccountResource(String serialNumber, String clinicId) {
}
