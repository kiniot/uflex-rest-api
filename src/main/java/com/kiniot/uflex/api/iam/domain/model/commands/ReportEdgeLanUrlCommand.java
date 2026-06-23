package com.kiniot.uflex.api.iam.domain.model.commands;

/**
 * Command for an edge service account to report its current LAN base URL.
 * <p>
 * The owning serial is resolved from the authenticated principal (never supplied by the
 * client), so an edge can only ever update its own URL (per-edge least-privilege).
 *
 * @param lanUrl the edge's reachable LAN base URL, e.g. {@code http://192.168.1.4:5050}
 */
public record ReportEdgeLanUrlCommand(String lanUrl) {
    public ReportEdgeLanUrlCommand {
        if (lanUrl == null || lanUrl.isBlank())
            throw new IllegalArgumentException("lanUrl cannot be null or blank");
    }
}
