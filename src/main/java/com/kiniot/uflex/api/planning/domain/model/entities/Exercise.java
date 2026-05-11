package com.kiniot.uflex.api.planning.domain.model.entities;

import com.kiniot.uflex.api.planning.domain.model.commands.CreateExerciseCommand;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.BodyPart;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseDescription;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseName;
import com.kiniot.uflex.api.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class Exercise extends AuditableModel<ExerciseId> {

    @EmbeddedId
    private ExerciseId id;

    @Embedded
    private ExerciseName name;

    @Embedded
    private ExerciseDescription description;

    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "body_part", nullable = false, length = 40))
    private BodyPart bodyPart;

    protected Exercise() {}

    public Exercise(CreateExerciseCommand command) {
        this.id = new ExerciseId();
        this.name = command.name();
        this.description = command.description();
        this.bodyPart = command.bodyPart();
    }

    @Override
    public ExerciseId getId() {
        return id;
    }
}
