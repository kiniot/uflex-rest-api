package com.kiniot.uflex.api.media.infrastructure.storage.supabase;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kiniot.uflex.api.media.domain.exceptions.MediaStorageException;
import com.kiniot.uflex.api.media.domain.model.valueobjects.SignedUpload;
import com.kiniot.uflex.api.media.domain.services.MediaStorageService;
import com.kiniot.uflex.api.media.infrastructure.storage.supabase.configuration.SupabaseStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.Map;

/**
 * Supabase Storage adapter implementing the {@link MediaStorageService} port via
 * the Supabase Storage REST API.
 * <p>
 * Authentication uses the {@code service_role} key (sent as both {@code apikey}
 * and {@code Authorization: Bearer}), so this code MUST stay server-side. Signed
 * URLs returned to clients are short-lived and scoped to a single object.
 *
 * @see <a href="https://supabase.com/docs/reference/api/storage">Supabase Storage API</a>
 */
@Service
public class SupabaseStorageService implements MediaStorageService {

    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageService.class);

    private final SupabaseStorageProperties properties;
    private final RestClient restClient;

    public SupabaseStorageService(SupabaseStorageProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .defaultHeader("apikey", properties.getServiceRoleKey())
                .defaultHeader("Authorization", "Bearer " + properties.getServiceRoleKey())
                .build();
    }

    @Override
    public SignedUpload createSignedUploadUrl(String bucket, String objectPath, String contentType) {
        var endpoint = "%s/storage/v1/object/upload/sign/%s/%s".formatted(baseUrl(), bucket, objectPath);
        try {
            SignUploadResponse response = restClient.post()
                    .uri(URI.create(endpoint))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of())
                    .retrieve()
                    .body(SignUploadResponse.class);

            if (response == null || response.url() == null || response.url().isBlank()) {
                throw new MediaStorageException("Supabase returned an empty signed upload URL");
            }
            // response.url() looks like: /object/upload/sign/<bucket>/<path>?token=<jwt>
            var absoluteUploadUrl = baseUrl() + "/storage/v1" + response.url();
            var token = extractToken(response.url());
            return new SignedUpload(absoluteUploadUrl, token, properties.getUploadUrlExpirySeconds());
        } catch (RestClientResponseException ex) {
            log.error("Supabase sign-upload failed ({}): {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MediaStorageException("Could not create signed upload URL for " + objectPath, ex);
        } catch (RestClientException ex) {
            throw new MediaStorageException("Could not reach Supabase Storage to sign upload for " + objectPath, ex);
        }
    }

    @Override
    public String createSignedDownloadUrl(String bucket, String objectPath, int expiresInSeconds) {
        var endpoint = "%s/storage/v1/object/sign/%s/%s".formatted(baseUrl(), bucket, objectPath);
        try {
            SignDownloadResponse response = restClient.post()
                    .uri(URI.create(endpoint))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("expiresIn", expiresInSeconds))
                    .retrieve()
                    .body(SignDownloadResponse.class);

            if (response == null || response.signedUrl() == null || response.signedUrl().isBlank()) {
                return null;
            }
            return baseUrl() + "/storage/v1" + response.signedUrl();
        } catch (RestClientException ex) {
            log.warn("Could not create signed download URL for {}: {}", objectPath, ex.getMessage());
            return null;
        }
    }

    @Override
    public void deleteObject(String bucket, String objectPath) {
        var endpoint = "%s/storage/v1/object/%s/%s".formatted(baseUrl(), bucket, objectPath);
        try {
            restClient.delete()
                    .uri(URI.create(endpoint))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return; // already gone — deletion is idempotent
            }
            log.error("Supabase delete failed ({}): {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MediaStorageException("Could not delete object " + objectPath, ex);
        } catch (RestClientException ex) {
            throw new MediaStorageException("Could not reach Supabase Storage to delete " + objectPath, ex);
        }
    }

    private String baseUrl() {
        var url = properties.getUrl();
        if (url == null || url.isBlank()) {
            throw new MediaStorageException("supabase.storage.url is not configured");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String extractToken(String urlWithQuery) {
        int idx = urlWithQuery.indexOf("token=");
        if (idx < 0) {
            return null;
        }
        var token = urlWithQuery.substring(idx + "token=".length());
        int amp = token.indexOf('&');
        return amp >= 0 ? token.substring(0, amp) : token;
    }

    /** Response of POST /object/upload/sign/{bucket}/{path}. */
    private record SignUploadResponse(String url) {}

    /** Response of POST /object/sign/{bucket}/{path}. */
    private record SignDownloadResponse(@JsonProperty("signedURL") String signedUrl) {}
}
