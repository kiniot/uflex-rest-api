package com.kiniot.uflex.api.therapy.interfaces.rest.transform;

import com.kiniot.uflex.api.therapy.domain.model.entities.CompletedRepetition;
import com.kiniot.uflex.api.therapy.interfaces.rest.resources.CompletedRepetitionResource;

public final class CompletedRepetitionResourceFromEntityAssembler {

    private CompletedRepetitionResourceFromEntityAssembler() {}

    public static CompletedRepetitionResource toResourceFromEntity(CompletedRepetition repetition) {
        return CompletedRepetitionResource.builder()
                .repetitionId(repetition.getId() != null ? repetition.getId().id() : null)
                .peakAngle(repetition.getPeakAngle())
                .achievedRom(repetition.getAchievedRom())
                .classification(repetition.getClassification() != null
                        ? repetition.getClassification().name() : null)
                .recordedAt(repetition.getRecordedAt())
                .build();
    }
}
