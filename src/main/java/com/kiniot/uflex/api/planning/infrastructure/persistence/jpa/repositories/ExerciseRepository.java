package com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.planning.domain.model.aggregates.Exercise;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.BodyPart;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseId;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.ExerciseName;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, ExerciseId> {

    Optional<Exercise> findByIdAndClinicId(ExerciseId id, ClinicId clinicId);

    Optional<Exercise> findByNameAndClinicId(ExerciseName name, ClinicId clinicId);

    List<Exercise> findAllByClinicId(ClinicId clinicId);

    List<Exercise> findAllByClinicIdAndBodyPart(ClinicId clinicId, BodyPart bodyPart);

    List<Exercise> findAllByClinicIdAndBodyPartAndNameContainingIgnoreCase(ClinicId clinicId, BodyPart bodyPart, String name);

    boolean existsByNameAndClinicId(ExerciseName name, ClinicId clinicId);
}
