package com.kiniot.uflex.api.iam.interfaces.acl;

import java.util.List;

public interface IamContextFacade {
    String fetchUserEmailAddressByUserId(String userId);

    String fetchAuthenticatedUserId();

    String fetchAuthenticatedUserTenantId();
}