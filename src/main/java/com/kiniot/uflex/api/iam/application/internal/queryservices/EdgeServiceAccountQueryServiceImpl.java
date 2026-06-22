package com.kiniot.uflex.api.iam.application.internal.queryservices;

import com.kiniot.uflex.api.iam.domain.model.aggregates.EdgeServiceAccount;
import com.kiniot.uflex.api.iam.domain.services.EdgeServiceAccountQueryService;
import com.kiniot.uflex.api.iam.infrastructure.persistence.jpa.repositories.EdgeServiceAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EdgeServiceAccountQueryServiceImpl implements EdgeServiceAccountQueryService {

    private final EdgeServiceAccountRepository edgeServiceAccountRepository;

    public EdgeServiceAccountQueryServiceImpl(EdgeServiceAccountRepository edgeServiceAccountRepository) {
        this.edgeServiceAccountRepository = edgeServiceAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EdgeServiceAccount> findAll() {
        return edgeServiceAccountRepository.findAll();
    }
}
