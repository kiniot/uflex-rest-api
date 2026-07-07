package com.kiniot.uflex.api.media.application.internal.commandservices;

import com.kiniot.uflex.api.media.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.media.domain.exceptions.MediaAssetNotFoundException;
import com.kiniot.uflex.api.media.domain.exceptions.MediaFileTooLargeException;
import com.kiniot.uflex.api.media.domain.exceptions.UnsupportedMediaContentTypeException;
import com.kiniot.uflex.api.media.domain.model.aggregates.MediaAsset;
import com.kiniot.uflex.api.media.domain.model.commands.ConfirmMediaUploadCommand;
import com.kiniot.uflex.api.media.domain.model.commands.CreateMediaUploadCommand;
import com.kiniot.uflex.api.media.domain.model.commands.DeleteMediaAssetCommand;
import com.kiniot.uflex.api.media.domain.model.results.MediaUploadTicket;
import com.kiniot.uflex.api.media.domain.model.valueobjects.MediaAssetId;
import com.kiniot.uflex.api.media.domain.model.valueobjects.MediaType;
import com.kiniot.uflex.api.media.domain.services.MediaAssetCommandService;
import com.kiniot.uflex.api.media.domain.services.MediaStorageService;
import com.kiniot.uflex.api.media.infrastructure.persistence.jpa.repositories.MediaAssetRepository;
import com.kiniot.uflex.api.media.infrastructure.storage.supabase.configuration.SupabaseStorageProperties;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserIdNotFoundException;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class MediaAssetCommandServiceImpl implements MediaAssetCommandService {

    private final MediaAssetRepository mediaAssetRepository;
    private final MediaStorageService mediaStorageService;
    private final ExternalIamService externalIamService;
    private final SupabaseStorageProperties properties;

    public MediaAssetCommandServiceImpl(
            MediaAssetRepository mediaAssetRepository,
            MediaStorageService mediaStorageService,
            ExternalIamService externalIamService,
            SupabaseStorageProperties properties
    ) {
        this.mediaAssetRepository = mediaAssetRepository;
        this.mediaStorageService = mediaStorageService;
        this.externalIamService = externalIamService;
        this.properties = properties;
    }

    @Override
    @Transactional
    public MediaUploadTicket handle(CreateMediaUploadCommand command) {
        validateContentType(command.mediaType(), command.contentType());
        validateSize(command.mediaType(), command.sizeBytes());

        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        var userId = externalIamService.fetchCurrentUserId()
                .orElseThrow(AuthenticatedUserIdNotFoundException::new);

        var asset = new MediaAsset(
                command.ownerType(),
                command.ownerId(),
                command.mediaType(),
                command.contentType(),
                command.originalFileName(),
                properties.getBucket(),
                deriveExtension(command.originalFileName(), command.contentType()),
                clinicId,
                userId
        );

        var signedUpload = mediaStorageService.createSignedUploadUrl(
                asset.getBucket(), asset.getObjectPath(), command.contentType());

        mediaAssetRepository.save(asset);
        return new MediaUploadTicket(asset, signedUpload);
    }

    @Override
    @Transactional
    public MediaAsset handle(ConfirmMediaUploadCommand command) {
        var asset = getOwnedAssetOrThrow(command.mediaAssetId());
        asset.confirmUpload(command.sizeBytes());
        return mediaAssetRepository.save(asset);
    }

    @Override
    @Transactional
    public void handle(DeleteMediaAssetCommand command) {
        var asset = getOwnedAssetOrThrow(command.mediaAssetId());
        mediaStorageService.deleteObject(asset.getBucket(), asset.getObjectPath());
        mediaAssetRepository.delete(asset);
    }

    private MediaAsset getOwnedAssetOrThrow(MediaAssetId mediaAssetId) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        return mediaAssetRepository.findById(mediaAssetId)
                .filter(asset -> asset.getClinicId().equals(clinicId))
                .orElseThrow(() -> new MediaAssetNotFoundException(mediaAssetId.id().toString()));
    }

    private void validateContentType(MediaType mediaType, String contentType) {
        var normalized = contentType.toLowerCase(Locale.ROOT).trim();
        boolean allowed = switch (mediaType) {
            case IMAGE -> properties.getAllowedImageContentTypes().contains(normalized);
            case VIDEO -> properties.getAllowedVideoContentTypes().contains(normalized);
        };
        if (!allowed) {
            throw new UnsupportedMediaContentTypeException(contentType);
        }
    }

    private void validateSize(MediaType mediaType, Long sizeBytes) {
        if (sizeBytes == null) {
            return;
        }
        long max = switch (mediaType) {
            case IMAGE -> properties.getMaxImageBytes();
            case VIDEO -> properties.getMaxVideoBytes();
        };
        if (sizeBytes > max) {
            throw new MediaFileTooLargeException(sizeBytes, max);
        }
    }

    /**
     * Derives a lowercase file extension, preferring the original file name and
     * falling back to the MIME subtype (e.g. video/mp4 -> mp4).
     */
    private String deriveExtension(String originalFileName, String contentType) {
        if (originalFileName != null) {
            int dot = originalFileName.lastIndexOf('.');
            if (dot >= 0 && dot < originalFileName.length() - 1) {
                return originalFileName.substring(dot + 1).toLowerCase(Locale.ROOT);
            }
        }
        if (contentType != null && contentType.contains("/")) {
            var subtype = contentType.substring(contentType.indexOf('/') + 1).toLowerCase(Locale.ROOT);
            return switch (subtype) {
                case "jpeg" -> "jpg";
                case "quicktime" -> "mov";
                default -> subtype;
            };
        }
        return "";
    }
}
