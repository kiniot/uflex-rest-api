package com.kiniot.uflex.api.media.domain.model.aggregates;

import com.kiniot.uflex.api.media.domain.exceptions.MediaUploadNotConfirmableException;
import com.kiniot.uflex.api.media.domain.model.events.MediaUploadConfirmedEvent;
import com.kiniot.uflex.api.media.domain.model.valueobjects.MediaAssetId;
import com.kiniot.uflex.api.media.domain.model.valueobjects.MediaStatus;
import com.kiniot.uflex.api.media.domain.model.valueobjects.MediaType;
import com.kiniot.uflex.api.media.domain.model.valueobjects.OwnerType;
import com.kiniot.uflex.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

/**
 * A media asset (image or video) stored in Supabase Storage and tracked in the
 * application database. Uploads follow the signed-URL flow: the asset is created
 * in {@link MediaStatus#PENDING}, the client uploads the bytes directly to Supabase
 * using a signed URL, and then confirms, moving the asset to {@link MediaStatus#UPLOADED}.
 * <p>
 * The owner is polymorphic ({@code ownerType} + {@code ownerId}) so this single
 * aggregate serves physiotherapist records, patient evidence, profile photos and
 * generic attachments without coupling to any existing context.
 */
@Getter
@Entity
public class MediaAsset extends AuditableAbstractAggregateRoot<MediaAsset, MediaAssetId> {

    @EmbeddedId
    private MediaAssetId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OwnerType ownerType;

    @Column(name = "owner_id", columnDefinition = "UUID")
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MediaType mediaType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MediaStatus status;

    @Column(nullable = false, length = 100)
    private String bucket;

    @Column(name = "object_path", nullable = false, length = 512)
    private String objectPath;

    @Column(name = "content_type", nullable = false, length = 150)
    private String contentType;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "clinic_id", columnDefinition = "UUID", nullable = false))
    private ClinicId clinicId;

    @Column(name = "uploaded_by_user_id", columnDefinition = "UUID", nullable = false)
    private UUID uploadedByUserId;

    protected MediaAsset() {}

    public MediaAsset(
            OwnerType ownerType,
            UUID ownerId,
            MediaType mediaType,
            String contentType,
            String originalFileName,
            String bucket,
            String fileExtension,
            ClinicId clinicId,
            UUID uploadedByUserId
    ) {
        this.id = new MediaAssetId();
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.mediaType = mediaType;
        this.contentType = contentType;
        this.originalFileName = originalFileName;
        this.bucket = bucket;
        this.clinicId = clinicId;
        this.uploadedByUserId = uploadedByUserId;
        this.status = MediaStatus.PENDING;
        this.objectPath = buildObjectPath(ownerType, ownerId, this.id.id(), fileExtension);
    }

    /**
     * Builds a deterministic, collision-free object key inside the bucket:
     * {@code <ownerType>/<ownerId|shared>/<assetId><.ext>}.
     */
    private static String buildObjectPath(OwnerType ownerType, UUID ownerId, UUID assetId, String fileExtension) {
        String owner = ownerId != null ? ownerId.toString() : "shared";
        String extension = (fileExtension != null && !fileExtension.isBlank())
                ? "." + fileExtension.replace(".", "").toLowerCase()
                : "";
        return "%s/%s/%s%s".formatted(ownerType.name().toLowerCase(), owner, assetId, extension);
    }

    /**
     * Confirms the upload succeeded. Only assets in {@link MediaStatus#PENDING} can be confirmed.
     */
    public void confirmUpload(Long sizeBytes) {
        if (this.status != MediaStatus.PENDING) {
            throw new MediaUploadNotConfirmableException(this.id.id().toString(), this.status.name());
        }
        this.status = MediaStatus.UPLOADED;
        if (sizeBytes != null) {
            this.sizeBytes = sizeBytes;
        }
        this.addDomainEvent(new MediaUploadConfirmedEvent(this, this.ownerType, this.ownerId, this.objectPath));
    }

    public void markFailed() {
        this.status = MediaStatus.FAILED;
    }

    public void assignOwner(OwnerType ownerType, UUID ownerId) {
        this.ownerType = ownerType;
        this.ownerId = ownerId;
    }

    public boolean isUploaded() {
        return this.status == MediaStatus.UPLOADED;
    }

    @Override
    public MediaAssetId getId() {
        return id;
    }
}
