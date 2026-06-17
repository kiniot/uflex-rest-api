package com.kiniot.uflex.api.therapy.domain.model.entities;

import com.kiniot.uflex.api.shared.domain.model.entities.AuditableModel;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.RoutineId;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.RoutineStatus;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SerieId;
import com.kiniot.uflex.api.therapy.domain.model.valueobjects.SerieStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class RoutineExecution extends AuditableModel<RoutineId> {

    @EmbeddedId
    private RoutineId id;

    @Column(columnDefinition = "UUID", nullable = false)
    private UUID planningRoutineId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoutineStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "routine_execution_id")
    private List<Serie> series = new ArrayList<>();

    public RoutineExecution(UUID planningRoutineId, List<Serie> series) {
        this.id = new RoutineId();
        this.planningRoutineId = planningRoutineId;
        this.status = RoutineStatus.Pending;
        this.series = series != null ? series : new ArrayList<>();
    }

    public void start() {
        this.status = RoutineStatus.InProgress;
    }

    public Optional<Serie> findSerie(SerieId serieId) {
        return series.stream().filter(s -> s.getId().equals(serieId)).findFirst();
    }

    public boolean isCompleted() {
        return !series.isEmpty() && series.stream().allMatch(s -> s.getStatus() == SerieStatus.Validated);
    }

    public void checkCompletion() {
        if (isCompleted()) {
            this.status = RoutineStatus.Completed;
        }
    }

    @Override
    public RoutineId getId() {
        return id;
    }
}
