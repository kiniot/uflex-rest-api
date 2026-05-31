package com.kiniot.uflex.api.planning.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.planning.domain.model.entities.Routine;
import com.kiniot.uflex.api.planning.domain.model.valueobjects.RoutineId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RoutineRepository extends JpaRepository<Routine, RoutineId> {

    @EntityGraph(attributePaths = {"exerciseSeries"})
    List<Routine> findAllByIdIn(Collection<RoutineId> ids);
}
