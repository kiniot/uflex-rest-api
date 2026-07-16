package com.kiniot.uflex.api.therapy.domain.model.queries;

import java.util.UUID;

/**
 * Lists a patient's therapy sessions for clinical follow-up, newest first.
 *
 * <p>{@code treatmentPlanId} is the only filter, and it is optional ({@code null} means "every
 * plan"): it is what the per-plan ROM trend needs. Narrowing by status or date is left to the
 * client, which already holds the whole history and does its filtering where the table lives.
 */
public record GetTherapySessionHistoryQuery(UUID patientId, UUID treatmentPlanId) {}
