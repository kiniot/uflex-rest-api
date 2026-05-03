package com.kiniot.uflex.api.iam.application.internal.outboundservices.identity;

import java.util.Optional;
import java.util.Set;

public interface IdentityService {

    Optional<String> getUserId();

    Optional<String> getEmail();

    Set<String> getRoles();

    Optional<String> getTenantId();

    default boolean isServiceAccount() {return false;}
}