package com.kiniot.uflex.api.media.application.internal.queryservices;

import com.kiniot.uflex.api.media.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.media.domain.model.aggregates.MediaAsset;
import com.kiniot.uflex.api.media.domain.model.queries.GetMediaAssetByIdQuery;
import com.kiniot.uflex.api.media.domain.model.queries.GetMediaAssetsByOwnerQuery;
import com.kiniot.uflex.api.media.domain.model.valueobjects.MediaStatus;
import com.kiniot.uflex.api.media.domain.services.MediaAssetQueryService;
import com.kiniot.uflex.api.media.infrastructure.persistence.jpa.repositories.MediaAssetRepository;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MediaAssetQueryServiceImpl implements MediaAssetQueryService {

    private final MediaAssetRepository mediaAssetRepository;
    private final ExternalIamService externalIamService;

    public MediaAssetQueryServiceImpl(
            MediaAssetRepository mediaAssetRepository,
            ExternalIamService externalIamService
    ) {
        this.mediaAssetRepository = mediaAssetRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaAsset> handle(GetMediaAssetByIdQuery query) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        return mediaAssetRepository.findById(query.mediaAssetId())
                .filter(asset -> asset.getClinicId().equals(clinicId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaAsset> handle(GetMediaAssetsByOwnerQuery query) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        if (query.ownerId() != null) {
            return mediaAssetRepository.findAllByOwnerTypeAndOwnerIdAndClinicIdAndStatusOrderByCreatedAtDesc(
                    query.ownerType(), query.ownerId(), clinicId, MediaStatus.UPLOADED);
        }
        return mediaAssetRepository.findAllByOwnerTypeAndClinicIdAndStatusOrderByCreatedAtDesc(
                query.ownerType(), clinicId, MediaStatus.UPLOADED);
    }
}
