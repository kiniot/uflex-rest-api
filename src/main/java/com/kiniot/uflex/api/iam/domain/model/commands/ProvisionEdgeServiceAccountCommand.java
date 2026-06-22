package com.kiniot.uflex.api.iam.domain.model.commands;

/**
 * Command to provision an edge service account (a {@code ROLE_EDGE} principal) bound
 * to the kit identified by {@code serialNumber}.
 * <p>
 * {@code clinicId} is the owning clinic: required when a {@code ROLE_DEVELOPER}
 * provisions (no tenant context), and optional for a {@code ROLE_CLINIC_ADMIN} (then
 * inferred from the caller's own clinic).
 *
 * @param serialNumber the cross-service serial of the kit this edge serves
 * @param clinicId     the owning clinic UUID, or {@code null} to infer it from the caller
 */
public record ProvisionEdgeServiceAccountCommand(String serialNumber, String clinicId) {
    public ProvisionEdgeServiceAccountCommand {
        if (serialNumber == null || serialNumber.isBlank())
            throw new IllegalArgumentException("serialNumber cannot be null or blank");
    }
}
