package com.kiniot.uflex.api.media.infrastructure.storage.supabase;

import com.kiniot.uflex.api.media.infrastructure.storage.supabase.configuration.SupabaseStorageProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SupabaseStorageServiceTests {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldReturnSignedTusConfigurationForVideoUpload() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/storage/v1/object/upload/sign/uflex-media/videos/demo.mp4", exchange -> {
            assertEquals("POST", exchange.getRequestMethod());
            var response = """
                    {
                      "url": "/object/upload/sign/uflex-media/videos/demo.mp4?token=signed-token",
                      "token": "signed-token"
                    }
                    """;
            var bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();

        var properties = new SupabaseStorageProperties();
        properties.setUrl("http://127.0.0.1:" + server.getAddress().getPort());
        properties.setServiceRoleKey("service-role-secret");
        properties.setAnonKey("publishable-key");

        var signedUpload = new SupabaseStorageService(properties)
                .createSignedUploadUrl("uflex-media", "videos/demo.mp4", "video/mp4");

        assertEquals("TUS_RESUMABLE", signedUpload.preferredStrategy());
        assertEquals(properties.getUrl() + "/storage/v1/upload/resumable/sign",
                signedUpload.resumableEndpoint());
        assertEquals("publishable-key", signedUpload.resumableHeaders().get("apikey"));
        assertEquals("signed-token", signedUpload.resumableHeaders().get("x-signature"));
        assertEquals("false", signedUpload.resumableHeaders().get("x-upsert"));
        assertEquals("uflex-media", signedUpload.resumableMetadata().get("bucketName"));
        assertEquals("videos/demo.mp4", signedUpload.resumableMetadata().get("objectName"));
        assertEquals("video/mp4", signedUpload.resumableMetadata().get("contentType"));
        assertEquals(6L * 1024 * 1024, signedUpload.resumableChunkSizeBytes());
    }
}
