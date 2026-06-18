package com.kiniot.uflex.api.media.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.media.domain.model.aggregates.MediaAsset;
import com.kiniot.uflex.api.media.domain.model.valueobjects.MediaAssetId;
import com.kiniot.uflex.api.media.domain.model.valueobjects.MediaStatus;
import com.kiniot.uflex.api.media.domain.model.valueobjects.OwnerType;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, MediaAssetId> {

    List<MediaAsset> findAllByOwnerTypeAndOwnerIdAndClinicIdAndStatusOrderByCreatedAtDesc(
            OwnerType ownerType, UUID ownerId, ClinicId clinicId, MediaStatus status);

    List<MediaAsset> findAllByOwnerTypeAndClinicIdAndStatusOrderByCreatedAtDesc(
            OwnerType ownerType, ClinicId clinicId, MediaStatus status);
}
