package com.kiniot.uflex.api.iam.domain.services;

import com.kiniot.uflex.api.iam.domain.model.aggregates.EdgeServiceAccount;

import java.util.List;

public interface EdgeServiceAccountQueryService {

    /** All provisioned edge service accounts (no credentials). */
    List<EdgeServiceAccount> findAll();
}
