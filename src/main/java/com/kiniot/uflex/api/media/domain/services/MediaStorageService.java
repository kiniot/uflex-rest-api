package com.kiniot.uflex.api.media.domain.services;

import com.kiniot.uflex.api.media.domain.model.valueobjects.SignedUpload;

/**
 * Domain port for the object storage provider. The default implementation talks
 * to Supabase Storage over its REST API using the {@code service_role} key, which
 * stays on the server side only. Clients never receive that key — they only get
 * short-lived signed URLs produced here.
 */
public interface MediaStorageService {

    /**
     * Asks the provider for a signed URL the client can PUT a file to directly.
     *
     * @param bucket      target bucket.
     * @param objectPath  object key inside the bucket.
     * @param contentType MIME type that will be uploaded.
     * @return absolute upload URL, token and TTL.
     */
    SignedUpload createSignedUploadUrl(String bucket, String objectPath, String contentType);

    /**
     * Creates a short-lived signed URL to download/view a private object.
     *
     * @return absolute, time-limited URL or {@code null} if it could not be created.
     */
    String createSignedDownloadUrl(String bucket, String objectPath, int expiresInSeconds);

    /**
     * Permanently removes the object from storage. Must not throw if the object
     * is already gone.
     */
    void deleteObject(String bucket, String objectPath);
}
