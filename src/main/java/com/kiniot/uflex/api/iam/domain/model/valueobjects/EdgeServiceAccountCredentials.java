package com.kiniot.uflex.api.iam.domain.model.valueobjects;

/**
 * Transient carrier for the plaintext credential of a freshly provisioned edge
 * service account. Returned once at provisioning time; the plaintext password is
 * never persisted and cannot be retrieved again.
 *
 * @param email        the synthetic login email of the edge account
 * @param password     the plaintext password (shown only once)
 * @param serialNumber the kit serial this edge account is bound to
 */
public record EdgeServiceAccountCredentials(String email, String password, String serialNumber) {
}
