package com.kiniot.uflex.api.therapy.domain.model.queries;

/**
 * Query that resolves the edge-connection info (LAN URL + pairing token) the authenticated
 * patient's mobile app needs to subscribe to its edge's live SSE progress stream.
 * <p>
 * Carries no parameters: the patient and their active session are resolved from the
 * authenticated principal.
 */
public record GetEdgeConnectionForCurrentPatientQuery() {}
