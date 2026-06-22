package com.kiniot.uflex.api.device.domain.model.queries;

/**
 * Cross-clinic device inventory overview, used by the platform developer console
 * (ROLE_DEVELOPER). Unlike {@link GetClinicFleetMetricsQuery}, it is not scoped to a clinic.
 */
public record GetGlobalDeviceOverviewQuery() {
}
