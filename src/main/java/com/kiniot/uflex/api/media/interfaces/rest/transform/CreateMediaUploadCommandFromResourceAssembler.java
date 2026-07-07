package com.kiniot.uflex.api.media.interfaces.rest.transform;

import com.kiniot.uflex.api.media.domain.model.commands.CreateMediaUploadCommand;
import com.kiniot.uflex.api.media.domain.model.valueobjects.MediaType;
import com.kiniot.uflex.api.media.domain.model.valueobjects.OwnerType;
import com.kiniot.uflex.api.media.interfaces.rest.resources.CreateMediaUploadResource;

import java.util.Locale;
import java.util.UUID;

public class CreateMediaUploadCommandFromResourceAssembler {

    private CreateMediaUploadCommandFromResourceAssembler() {}

    public static CreateMediaUploadCommand toCommandFromResource(CreateMediaUploadResource resource) {
        if (resource.ownerType() == null || resource.ownerType().isBlank()) {
            throw new IllegalArgumentException("ownerType is required");
        }
        if (resource.mediaType() == null || resource.mediaType().isBlank()) {
            throw new IllegalArgumentException("mediaType is required");
        }
        var ownerType = OwnerType.valueOf(resource.ownerType().toUpperCase(Locale.ROOT));
        var mediaType = MediaType.valueOf(resource.mediaType().toUpperCase(Locale.ROOT));
        var ownerId = (resource.ownerId() != null && !resource.ownerId().isBlank())
                ? UUID.fromString(resource.ownerId())
                : null;
        return new CreateMediaUploadCommand(
                ownerType,
                ownerId,
                mediaType,
                resource.contentType(),
                resource.fileName(),
                resource.sizeBytes()
        );
    }
}
