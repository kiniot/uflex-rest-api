package com.kiniot.uflex.api.organization.application.internal.queryservices;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.exceptions.UserNotFoundException;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Physiotherapist;
import com.kiniot.uflex.api.organization.domain.model.queries.GetCurrentPhysiotherapistQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPhysiotherapistByIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPhysiotherapistByUserIdQuery;
import com.kiniot.uflex.api.organization.domain.model.queries.GetPhysiotherapistsByClinicIdQuery;
import com.kiniot.uflex.api.organization.domain.services.PhysiotherapistQueryService;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.PhysiotherapistRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PhysiotherapistQueryServiceImpl implements PhysiotherapistQueryService {

    private final PhysiotherapistRepository physiotherapistRepository;
    private final ExternalIamService externalIamService;

    public PhysiotherapistQueryServiceImpl(
            PhysiotherapistRepository physiotherapistRepository,
            ExternalIamService externalIamService
    ) {
        this.physiotherapistRepository = physiotherapistRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    public Optional<Physiotherapist> handle(GetCurrentPhysiotherapistQuery query) {
        var userId = externalIamService.fetchCurrentUserId()
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
        return physiotherapistRepository.findByUserId(userId);
    }

    @Override
    public Optional<Physiotherapist> handle(GetPhysiotherapistByIdQuery query) {
        return physiotherapistRepository.findById(query.physiotherapistId());
    }

    @Override
    public Optional<Physiotherapist> handle(GetPhysiotherapistByUserIdQuery query) {
        return physiotherapistRepository.findByUserId(query.userId());
    }

    @Override
    public List<Physiotherapist> handle(GetPhysiotherapistsByClinicIdQuery query) {
        return physiotherapistRepository.findAllByClinicId(query.clinicId());
    }
}
