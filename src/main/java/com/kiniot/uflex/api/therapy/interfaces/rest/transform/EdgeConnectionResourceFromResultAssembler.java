package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.queries.EdgeConnection;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.EdgeConnectionResource;

public final class EdgeConnectionResourceFromResultAssembler {

    private EdgeConnectionResourceFromResultAssembler() {}

    public static EdgeConnectionResource toResourceFromResult(EdgeConnection result) {
        return new EdgeConnectionResource(
                result.localEdgeUrl(),
                result.pairingToken(),
                result.expiresAt()
        );
    }
}
