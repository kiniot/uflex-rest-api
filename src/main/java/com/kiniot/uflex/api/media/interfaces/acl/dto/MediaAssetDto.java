package com.kiniot.uflex.api.media.interfaces.acl.dto;

public record MediaAssetDto(
        String id,
        String clinicId,
        String ownerType,
        String ownerId,
        String mediaType,
        String status,
        String contentType,
        String originalFileName,
        Long sizeBytes
) {
}
