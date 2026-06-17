package com.kiniot.uflex.api.iam.interfaces.acl;

import java.util.List;

public interface IamContextFacade {
    String fetchUserEmailAddressByUserId(String userId);

    String signUpVerifiedUser(String email, List<String> roles);

    void updateUserEmail(String userId, String email);

    void deleteUserById(String userId);

    String fetchContextUserId();

    String fetchContextTenantId();

    String fetchCurrentUserId();

    String fetchCurrentTenantId();
}
