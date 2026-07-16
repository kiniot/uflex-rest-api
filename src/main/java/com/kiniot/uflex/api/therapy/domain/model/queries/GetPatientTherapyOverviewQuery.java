package com.kiniot.uflex.api.therapy.domain.model.queries;

/**
 * Therapy standing of every patient assigned to the authenticated physiotherapist: the index they
 * land on before drilling into one patient's history.
 *
 * <p>Carries no parameters — the physiotherapist and their clinic both come from the security
 * context, so a caller cannot ask about someone else's caseload.
 */
public record GetPatientTherapyOverviewQuery() {}
