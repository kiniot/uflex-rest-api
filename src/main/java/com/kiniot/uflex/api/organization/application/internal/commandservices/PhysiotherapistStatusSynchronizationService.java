package com.kiniot.uflex.api.organization.application.internal.commandservices;

import com.kiniot.uflex.api.organization.domain.model.aggregates.Patient;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Physiotherapist;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PatientStatus;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.PatientRepository;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.PhysiotherapistRepository;
import org.springframework.stereotype.Service;

@Service
public class PhysiotherapistStatusSynchronizationService {

    private final PatientRepository patientRepository;
    private final PhysiotherapistRepository physiotherapistRepository;

    public PhysiotherapistStatusSynchronizationService(
            PatientRepository patientRepository,
            PhysiotherapistRepository physiotherapistRepository
    ) {
        this.patientRepository = patientRepository;
        this.physiotherapistRepository = physiotherapistRepository;
    }

    public Physiotherapist synchronize(Physiotherapist physiotherapist) {
        physiotherapist.synchronizeAvailability(hasAssignedPatientsInCharge(physiotherapist));
        return physiotherapistRepository.save(physiotherapist);
    }

    public Physiotherapist reactivate(Physiotherapist physiotherapist) {
        physiotherapist.reactivate(hasAssignedPatientsInCharge(physiotherapist));
        return physiotherapistRepository.save(physiotherapist);
    }

    private boolean hasAssignedPatientsInCharge(Physiotherapist physiotherapist) {
        return patientRepository.findAllByAssignedPhysiotherapistId(physiotherapist.getId()).stream()
                .anyMatch(this::countsForAvailability);
    }

    private boolean countsForAvailability(Patient patient) {
        return patient.getStatus() == PatientStatus.IN_TREATMENT
                || patient.getStatus() == PatientStatus.COMPLETED;
    }
}
