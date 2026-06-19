package com.kiniot.uflex.api.planning.domain.model.aggregates;

import com.kiniot.uflex.api.planning.domain.model.commands.CreateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.commands.UpdateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.BodyPart;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseDescription;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseName;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.MovementType;
import com.kiniot.uflex.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Getter
@Entity
public class Exercise extends AuditableAbstractAggregateRoot<Exercise, ExerciseId> {

    @EmbeddedId
    private ExerciseId id;

    @Embedded
    private ExerciseName name;

    @Embedded
    private ExerciseDescription description;

    @Enumerated(EnumType.STRING)
    @Column(name = "body_part", nullable = false, length = 40)
    private BodyPart bodyPart;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 40)
    private MovementType movementType;

    @Column(name = "video_asset_id", columnDefinition = "UUID")
    private UUID videoAssetId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "clinic_id", columnDefinition = "UUID", nullable = false))
    private ClinicId clinicId;

    protected Exercise() {}

    public Exercise(CreateExerciseCommand command, ClinicId clinicId) {
        this.id = new ExerciseId();
        this.name = command.name();
        this.description = command.description();
        this.bodyPart = command.bodyPart();
        this.movementType = command.movementType();
        this.videoAssetId = command.videoAssetId();
        this.clinicId = clinicId;
    }

    public void update(UpdateExerciseCommand command) {
        this.name = command.name();
        this.description = command.description();
        this.bodyPart = command.bodyPart();
        this.movementType = command.movementType();
        this.videoAssetId = command.videoAssetId();
    }

    @Override
    public ExerciseId getId() {
        return id;
    }
}
