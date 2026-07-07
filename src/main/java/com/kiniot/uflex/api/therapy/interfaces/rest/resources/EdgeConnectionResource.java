package com.kiniot.uflex.api.therapy.interfaces.rest.resources;

import java.time.Instant;

/**
 * Edge-connection info for the patient's mobile app: where to reach the edge on the LAN and the
 * pairing token to present when subscribing to its live SSE progress stream.
 */
public record EdgeConnectionResource(
        String localEdgeUrl,
        String pairingToken,
        Instant expiresAt
) {}
