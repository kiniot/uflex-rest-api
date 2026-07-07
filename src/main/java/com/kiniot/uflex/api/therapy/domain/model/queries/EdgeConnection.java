package com.kiniot.uflex.api.therapy.domain.model.queries;

import java.time.Instant;

/**
 * Read-model result of {@link GetEdgeConnectionForCurrentPatientQuery}: where the patient's
 * edge can be reached on the LAN and the token to authenticate the SSE subscription.
 *
 * @param localEdgeUrl the edge's last reported LAN base URL, or {@code null} when the edge has
 *                     not reported in yet (the client then falls back to its build-time default)
 * @param pairingToken the opaque token tied to the active session; presented as a Bearer token
 * @param expiresAt    reserved for a future fixed TTL; {@code null} today (the token is valid while
 *                     the session stays active)
 */
public record EdgeConnection(String localEdgeUrl, String pairingToken, Instant expiresAt) {}
