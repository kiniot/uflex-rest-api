package com.kiniot.uflex.api.planning.application.internal.commandservices;

import com.kiniot.uflex.api.media.interfaces.acl.dto.MediaAssetDto;
import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalMediaService;
import com.kiniot.uflex.api.planning.domain.exceptions.ExerciseVideoAssetInvalidException;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.BodyPart;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseDescription;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.MovementType;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.ExerciseRepository;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExerciseCommandServiceImplTests {

    private ExerciseRepository exerciseRepository;
    private ExternalIamService externalIamService;
    private ExternalMediaService externalMediaService;
    private ExerciseCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        exerciseRepository = mock(ExerciseRepository.class);
        externalIamService = mock(ExternalIamService.class);
        externalMediaService = mock(ExternalMediaService.class);
        service = new ExerciseCommandServiceImpl(
                exerciseRepository,
                externalIamService,
                externalMediaService
        );
    }

    @Test
    void createExerciseAssignsUploadedVideoAsset() {
        var clinicId = new ClinicId(UUID.randomUUID());
        var videoAssetId = UUID.randomUUID();
        when(externalIamService.fetchCurrentClinicId()).thenReturn(Optional.of(clinicId));
        when(externalMediaService.findMediaAssetById(videoAssetId))
                .thenReturn(Optional.of(new MediaAssetDto(
                        videoAssetId.toString(),
                        clinicId.id().toString(),
                        "GENERIC",
                        null,
                        "VIDEO",
                        "UPLOADED",
                        "video/mp4",
                        "exercise.mp4",
                        123L
                )));
        when(exerciseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.handle(createCommand(videoAssetId));

        verify(externalMediaService).assignExerciseVideo(any(), any());
        verify(exerciseRepository).save(any());
    }

    @Test
    void createExerciseRejectsNonVideoAsset() {
        var clinicId = new ClinicId(UUID.randomUUID());
        var videoAssetId = UUID.randomUUID();
        when(externalIamService.fetchCurrentClinicId()).thenReturn(Optional.of(clinicId));
        when(externalMediaService.findMediaAssetById(videoAssetId))
                .thenReturn(Optional.of(new MediaAssetDto(
                        videoAssetId.toString(),
                        clinicId.id().toString(),
                        "GENERIC",
                        null,
                        "IMAGE",
                        "UPLOADED",
                        "image/jpeg",
                        "photo.jpg",
                        123L
                )));

        assertThrows(ExerciseVideoAssetInvalidException.class, () -> service.handle(createCommand(videoAssetId)));
    }

    private CreateExerciseCommand createCommand(UUID videoAssetId) {
        return new CreateExerciseCommand(
                new ExerciseName("Wrist supination"),
                new ExerciseDescription("Controlled wrist supination exercise."),
                BodyPart.WRIST,
                MovementType.SUPINATION,
                videoAssetId
        );
    }
}
