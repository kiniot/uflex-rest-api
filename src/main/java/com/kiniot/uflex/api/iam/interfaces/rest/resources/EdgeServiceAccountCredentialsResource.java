package com.kiniot.uflex.api.iam.interfaces.rest.resources;

/**
 * Response returned once after provisioning an edge service account. The plaintext
 * {@code password} is shown only here and cannot be retrieved again.
 *
 * @param email        the synthetic login email of the edge account
 * @param password     the plaintext password (shown only once)
 * @param serialNumber the kit serial this edge account is bound to
 */
public record EdgeServiceAccountCredentialsResource(String email, String password, String serialNumber) {
}
