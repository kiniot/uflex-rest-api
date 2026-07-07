package com.kiniot.uflex.api.iam.interfaces.rest.resources;

/**
 * Request body for an edge reporting its current LAN base URL.
 *
 * @param lanUrl the edge's reachable LAN base URL, e.g. {@code http://192.168.1.4:5050}
 */
public record ReportEdgeLanUrlResource(String lanUrl) {}
