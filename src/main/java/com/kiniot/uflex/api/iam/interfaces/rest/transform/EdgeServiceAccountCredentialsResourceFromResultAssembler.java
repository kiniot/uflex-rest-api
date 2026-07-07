package com.kiniot.uflex.api.iam.interfaces.rest.transform;

import com.kiniot.uflex.api.iam.domain.model.valueobjects.EdgeServiceAccountCredentials;
import com.kiniot.uflex.api.iam.interfaces.rest.resources.EdgeServiceAccountCredentialsResource;

public class EdgeServiceAccountCredentialsResourceFromResultAssembler {

    public static EdgeServiceAccountCredentialsResource toResourceFromResult(EdgeServiceAccountCredentials credentials) {
        return new EdgeServiceAccountCredentialsResource(
                credentials.email(),
                credentials.password(),
                credentials.serialNumber());
    }
}
