package com.kiniot.uflex.api.iam.interfaces.acl;

import java.util.List;
import java.util.Optional;

public interface IamContextFacade {
    String fetchUserEmailAddressByUserId(String userId);

    /**
     * Returns the kit serial bound to the current authenticated principal when it is an
     * edge service account; empty otherwise (e.g. a human user).
     */
    Optional<String> findEdgeSerialForCurrentUser();

    /**
     * Returns the last LAN base URL reported by the edge bound to {@code serialNumber}, or
     * empty when no edge account exists for that serial or it has not reported a URL yet.
     */
    Optional<String> findEdgeLanUrlBySerial(String serialNumber);

    String signUpVerifiedUser(String email, List<String> roles);

    void updateUserEmail(String userId, String email);

    void deleteUserById(String userId);

    String fetchContextUserId();

    String fetchContextTenantId();

    String fetchCurrentUserId();

    String fetchCurrentTenantId();
}
