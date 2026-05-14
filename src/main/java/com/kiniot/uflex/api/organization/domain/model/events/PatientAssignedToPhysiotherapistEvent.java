package com.kiniot.uflex.api.organization.domain.model.events;

import com.kiniot.uflex.api.organization.domain.model.valueobjects.PatientId;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhysiotherapistId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class PatientAssignedToPhysiotherapistEvent extends ApplicationEvent {

    private final PatientId patientId;
    private final PhysiotherapistId physiotherapistId;
    private final ClinicId clinicId;
    private final Instant occurredOn;

    public PatientAssignedToPhysiotherapistEvent(Object source, PatientId patientId,
                                                  PhysiotherapistId physiotherapistId, ClinicId clinicId) {
        super(source);
        this.patientId = patientId;
        this.physiotherapistId = physiotherapistId;
        this.clinicId = clinicId;
        this.occurredOn = Instant.now();
    }
}
