package com.kiniot.uflex.api.iam.interfaces.rest.resources;

/** Edge service account summary (no credentials — the password is shown only at provisioning). */
public record EdgeServiceAccountResource(
        String id,
        String serialNumber,
        String clinicId,
        String createdAt
) {
}
