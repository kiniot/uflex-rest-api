package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.commands.ReportPainLevelCommand;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.ReportPainLevelResource;

import java.util.UUID;

public final class ReportPainLevelCommandFromResourceAssembler {

    private ReportPainLevelCommandFromResourceAssembler() {}

    public static ReportPainLevelCommand toCommandFromResource(UUID sessionId, ReportPainLevelResource resource) {
        return new ReportPainLevelCommand(sessionId, resource.painLevel());
    }
}
