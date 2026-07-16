package com.kiniot.uflex.api.therapy.domain.model.queries;

import java.util.UUID;

/**
 * Full inspection view of one session: series, their repetitions, and compensatory movements.
 *
 * <p>Unlike {@link GetSessionSummaryQuery}, this resolves in any status. The summary is the
 * patient-facing final clinical record and refuses to answer while a session is still running;
 * this query backs the clinician's live drill-down, which must work mid-session.
 */
public record GetTherapySessionDetailQuery(UUID sessionId) {}
