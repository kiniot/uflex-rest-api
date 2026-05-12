package com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.planning.domain.model.entities.Exercise;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.BodyPart;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, ExerciseId> {

    Optional<Exercise> findByName(ExerciseName name);

    List<Exercise> findAllByBodyPart(BodyPart bodyPart);

    List<Exercise> findAllByBodyPartAndNameContainingIgnoreCase(BodyPart bodyPart, String name);

    boolean existsByName(ExerciseName name);
}
