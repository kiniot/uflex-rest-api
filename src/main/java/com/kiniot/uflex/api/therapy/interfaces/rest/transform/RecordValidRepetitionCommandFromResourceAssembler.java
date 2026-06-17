package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.commands.RecordValidRepetitionCommand;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.RecordValidRepetitionResource;

import java.util.UUID;

public final class RecordValidRepetitionCommandFromResourceAssembler {

    private RecordValidRepetitionCommandFromResourceAssembler() {}

    public static RecordValidRepetitionCommand toCommandFromResource(
            UUID sessionId, UUID serieId, UUID edgeSequenceId, RecordValidRepetitionResource resource) {
        return new RecordValidRepetitionCommand(
                sessionId,
                serieId,
                resource.achievedAngle(),
                resource.recordedAt(),
                edgeSequenceId
        );
    }
}
