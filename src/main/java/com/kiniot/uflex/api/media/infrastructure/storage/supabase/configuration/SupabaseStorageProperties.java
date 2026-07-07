package com.kiniot.uflex.api.media.infrastructure.storage.supabase.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration for Supabase Storage. Bound from the {@code supabase.storage}
 * prefix in application.yaml. The {@code serviceRoleKey} is a secret and must
 * only ever live on the backend (never shipped to web/mobile clients).
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "supabase.storage")
public class SupabaseStorageProperties {

    /** Base Supabase project URL, e.g. https://gjapoeuqwcnmidswhrgu.supabase.co */
    private String url = "";

    /** Supabase service_role secret key (server-side only). */
    private String serviceRoleKey = "";

    /** Private bucket where all media is stored. */
    private String bucket = "uflex-media";

    /** TTL for signed upload URLs handed to clients. */
    private int uploadUrlExpirySeconds = 120;

    /** TTL for signed download URLs handed to clients. */
    private int downloadUrlExpirySeconds = 3600;

    /** Maximum allowed image size in bytes (default 10 MB). */
    private long maxImageBytes = 10L * 1024 * 1024;

    /** Maximum allowed video size in bytes (default 500 MB). */
    private long maxVideoBytes = 500L * 1024 * 1024;

    private List<String> allowedImageContentTypes = List.of(
            "image/jpeg", "image/png", "image/webp", "image/heic", "image/heif");

    private List<String> allowedVideoContentTypes = List.of(
            "video/mp4", "video/quicktime", "video/webm");
}
