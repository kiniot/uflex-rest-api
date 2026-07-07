package com.kiniot.uflex.api.media.interfaces.rest.controllers;

import com.kiniot.uflex.api.media.domain.model.aggregates.MediaAsset;
import com.kiniot.uflex.api.media.domain.model.commands.ConfirmMediaUploadCommand;
import com.kiniot.uflex.api.media.domain.model.commands.DeleteMediaAssetCommand;
import com.kiniot.uflex.api.media.domain.model.queries.GetMediaAssetByIdQuery;
import com.kiniot.uflex.api.media.domain.model.queries.GetMediaAssetsByOwnerQuery;
import com.kiniot.uflex.api.media.domain.model.valueobjects.MediaAssetId;
import com.kiniot.uflex.api.media.domain.model.valueobjects.OwnerType;
import com.kiniot.uflex.api.media.domain.services.MediaAssetCommandService;
import com.kiniot.uflex.api.media.domain.services.MediaAssetQueryService;
import com.kiniot.uflex.api.media.domain.services.MediaStorageService;
import com.kiniot.uflex.api.media.infrastructure.storage.supabase.configuration.SupabaseStorageProperties;
import com.kiniot.uflex.api.media.interfaces.rest.resources.ConfirmMediaUploadResource;
import com.kiniot.uflex.api.media.interfaces.rest.resources.CreateMediaUploadResource;
import com.kiniot.uflex.api.media.interfaces.rest.resources.MediaAssetResource;
import com.kiniot.uflex.api.media.interfaces.rest.resources.MediaUploadTicketResource;
import com.kiniot.uflex.api.media.interfaces.rest.transform.CreateMediaUploadCommandFromResourceAssembler;
import com.kiniot.uflex.api.media.interfaces.rest.transform.MediaAssetResourceFromEntityAssembler;
import com.kiniot.uflex.api.media.interfaces.rest.transform.MediaUploadTicketResourceFromResultAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Media upload/download endpoints implementing the signed-URL flow against
 * Supabase Storage:
 * <ol>
 *     <li>POST /uploads — create a PENDING asset and get a signed upload URL.</li>
 *     <li>Client PUTs the file directly to Supabase using that URL.</li>
 *     <li>POST /uploads/{id}/confirm — mark the asset as UPLOADED.</li>
 *     <li>GET /{id} or GET /?ownerType=... — read assets with a fresh signed download URL.</li>
 *     <li>DELETE /{id} — remove from storage and database.</li>
 * </ol>
 */
@RestController
@RequestMapping(value = "/api/v1/media", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Media", description = "Media (image/video) upload and retrieval endpoints")
public class MediaController {

    private final MediaAssetCommandService mediaAssetCommandService;
    private final MediaAssetQueryService mediaAssetQueryService;
    private final MediaStorageService mediaStorageService;
    private final SupabaseStorageProperties properties;

    public MediaController(
            MediaAssetCommandService mediaAssetCommandService,
            MediaAssetQueryService mediaAssetQueryService,
            MediaStorageService mediaStorageService,
            SupabaseStorageProperties properties
    ) {
        this.mediaAssetCommandService = mediaAssetCommandService;
        this.mediaAssetQueryService = mediaAssetQueryService;
        this.mediaStorageService = mediaStorageService;
        this.properties = properties;
    }

    @PostMapping("/uploads")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    @Operation(summary = "Create an upload ticket",
            description = "Creates a pending media asset and returns a short-lived signed URL to upload the file directly to Supabase Storage.")
    public ResponseEntity<MediaUploadTicketResource> createUpload(@RequestBody CreateMediaUploadResource resource) {
        var command = CreateMediaUploadCommandFromResourceAssembler.toCommandFromResource(resource);
        var ticket = mediaAssetCommandService.handle(command);
        return new ResponseEntity<>(
                MediaUploadTicketResourceFromResultAssembler.toResourceFromResult(ticket),
                HttpStatus.CREATED);
    }

    @PostMapping("/uploads/{mediaAssetId}/confirm")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    @Operation(summary = "Confirm an upload",
            description = "Marks the media asset as UPLOADED once the file has been stored in Supabase Storage.")
    public ResponseEntity<MediaAssetResource> confirmUpload(
            @PathVariable String mediaAssetId,
            @RequestBody(required = false) ConfirmMediaUploadResource resource
    ) {
        var sizeBytes = resource != null ? resource.sizeBytes() : null;
        var command = new ConfirmMediaUploadCommand(toMediaAssetId(mediaAssetId), sizeBytes);
        var asset = mediaAssetCommandService.handle(command);
        return ResponseEntity.ok(toResource(asset));
    }

    @GetMapping("/{mediaAssetId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    @Operation(summary = "Get a media asset",
            description = "Returns the media asset metadata together with a fresh, short-lived signed download URL.")
    public ResponseEntity<MediaAssetResource> getMediaAsset(@PathVariable String mediaAssetId) {
        return mediaAssetQueryService.handle(new GetMediaAssetByIdQuery(toMediaAssetId(mediaAssetId)))
                .map(this::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    @Operation(summary = "List media assets by owner",
            description = "Returns uploaded media assets attached to a given owner (e.g. a physiotherapist record or a patient), each with a fresh signed download URL.")
    public ResponseEntity<List<MediaAssetResource>> getMediaAssetsByOwner(
            @RequestParam String ownerType,
            @RequestParam(required = false) String ownerId
    ) {
        var resolvedOwnerType = OwnerType.valueOf(ownerType.toUpperCase(Locale.ROOT));
        var resolvedOwnerId = (ownerId != null && !ownerId.isBlank()) ? UUID.fromString(ownerId) : null;
        var assets = mediaAssetQueryService.handle(new GetMediaAssetsByOwnerQuery(resolvedOwnerType, resolvedOwnerId));
        var resources = assets.stream().map(this::toResource).toList();
        return ResponseEntity.ok(resources);
    }

    @DeleteMapping("/{mediaAssetId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_PHYSIOTHERAPIST', 'ROLE_PATIENT')")
    @Operation(summary = "Delete a media asset",
            description = "Removes the object from Supabase Storage and deletes its database record.")
    public ResponseEntity<Void> deleteMediaAsset(@PathVariable String mediaAssetId) {
        mediaAssetCommandService.handle(new DeleteMediaAssetCommand(toMediaAssetId(mediaAssetId)));
        return ResponseEntity.noContent().build();
    }

    private MediaAssetResource toResource(MediaAsset asset) {
        String downloadUrl = asset.isUploaded()
                ? mediaStorageService.createSignedDownloadUrl(
                        asset.getBucket(), asset.getObjectPath(), properties.getDownloadUrlExpirySeconds())
                : null;
        return MediaAssetResourceFromEntityAssembler.toResourceFromEntity(asset, downloadUrl);
    }

    private MediaAssetId toMediaAssetId(String mediaAssetId) {
        return new MediaAssetId(UUID.fromString(mediaAssetId));
    }
}
