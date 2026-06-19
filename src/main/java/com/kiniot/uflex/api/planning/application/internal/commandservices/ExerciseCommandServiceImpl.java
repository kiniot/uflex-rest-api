package com.kiniot.uflex.api.planning.application.internal.commandservices;

import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.planning.application.internal.outboundservices.acl.ExternalMediaService;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.planning.domain.exceptions.ExerciseVideoAssetInvalidException;
import com.kiniot.uflex.api.planning.domain.exceptions.ExerciseWithIdNotFoundException;
import com.kiniot.uflex.api.planning.domain.model.commands.CreateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.RemoveExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.UpdateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.aggregates.Exercise;
import com.kiniot.uflex.api.planning.domain.services.ExerciseCommandService;
import com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories.ExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
public class ExerciseCommandServiceImpl implements ExerciseCommandService {

    private final ExerciseRepository exerciseRepository;
    private final ExternalIamService externalIamService;
    private final ExternalMediaService externalMediaService;

    public ExerciseCommandServiceImpl(
            ExerciseRepository exerciseRepository,
            ExternalIamService externalIamService,
            ExternalMediaService externalMediaService
    ) {
        this.exerciseRepository = exerciseRepository;
        this.externalIamService = externalIamService;
        this.externalMediaService = externalMediaService;
    }

    @Override
    @Transactional
    public Optional<Exercise> handle(CreateExerciseCommand command) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        var exercise = new Exercise(command, clinicId);
        validateVideoAsset(command.videoAssetId(), clinicId.id().toString());
        if (command.videoAssetId() != null) {
            externalMediaService.assignExerciseVideo(command.videoAssetId(), exercise.getId().id());
        }
        return Optional.of(exerciseRepository.save(exercise));
    }

    @Override
    @Transactional
    public Optional<Exercise> handle(UpdateExerciseCommand command) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        var exercise = exerciseRepository.findByIdAndClinicId(command.exerciseId(), clinicId)
                .orElseThrow(() -> new ExerciseWithIdNotFoundException(command.exerciseId().id().toString()));
        boolean videoIsChanging = !Objects.equals(command.videoAssetId(), exercise.getVideoAssetId());
        if (videoIsChanging && command.videoAssetId() != null) {
            validateVideoAsset(command.videoAssetId(), clinicId.id().toString());
        }
        exercise.update(command);
        if (videoIsChanging && command.videoAssetId() != null) {
            externalMediaService.assignExerciseVideo(command.videoAssetId(), exercise.getId().id());
        }
        return Optional.of(exerciseRepository.save(exercise));
    }

    @Override
    @Transactional
    public void handle(RemoveExerciseCommand command) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        var exercise = exerciseRepository.findByIdAndClinicId(command.exerciseId(), clinicId)
                .orElseThrow(() -> new ExerciseWithIdNotFoundException(command.exerciseId().id().toString()));
        exerciseRepository.delete(exercise);
    }

    private void validateVideoAsset(java.util.UUID videoAssetId, String clinicId) {
        if (videoAssetId == null) {
            return;
        }
        var asset = externalMediaService.findMediaAssetById(videoAssetId)
                .orElseThrow(() -> new ExerciseVideoAssetInvalidException(
                        "Video asset not found: " + videoAssetId));
        if (!clinicId.equals(asset.clinicId())) {
            throw new ExerciseVideoAssetInvalidException(
                    "Video asset does not belong to the authenticated clinic");
        }
        if (!"UPLOADED".equals(asset.status())) {
            throw new ExerciseVideoAssetInvalidException(
                    "Video asset must be in UPLOADED status");
        }
        if (!"VIDEO".equals(asset.mediaType())) {
            throw new ExerciseVideoAssetInvalidException(
                    "Video asset must be a VIDEO");
        }
    }
}
