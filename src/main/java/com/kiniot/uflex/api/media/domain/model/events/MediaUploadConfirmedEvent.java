package com.kiniot.uflex.api.media.domain.model.events;

import com.kiniot.uflex.api.media.domain.model.valueobjects.OwnerType;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Published when a media asset upload is confirmed. Other contexts can listen to
 * this (e.g. to set a profile photo URL) without depending on the media context.
 */
public class MediaUploadConfirmedEvent extends ApplicationEvent {

    private final OwnerType ownerType;
    private final UUID ownerId;
    private final String objectPath;

    public MediaUploadConfirmedEvent(Object source, OwnerType ownerType, UUID ownerId, String objectPath) {
        super(source);
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.objectPath = objectPath;
    }

    public OwnerType getOwnerType() {
        return ownerType;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getObjectPath() {
        return objectPath;
    }
}
