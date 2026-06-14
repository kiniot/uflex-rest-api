package com.kiniot.uflex.api.organization.domain.model.aggregates;

import com.kiniot.uflex.api.organization.domain.exceptions.PatientOperationNotAllowedException;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.BirthDate;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.Dni;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.FirstName;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.Gender;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.LastName;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.MedicalCondition;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PatientStatus;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhoneNumber;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PatientTests {

    @Test
    void unassignPhysiotherapistKeepsClinicalStatus() {
        var clinicId = new ClinicId();
        var patient = createPatient(clinicId);
        patient.assignPhysiotherapist(new PhysiotherapistId(), clinicId);

        patient.unassignPhysiotherapist();

        assertNull(patient.getAssignedPhysiotherapistId());
        assertEquals(PatientStatus.IN_TREATMENT, patient.getStatus());
    }

    @Test
    void assignPhysiotherapistAllowsReassignmentWithinSameClinic() {
        var clinicId = new ClinicId();
        var patient = createPatient(clinicId);
        var firstPhysiotherapistId = new PhysiotherapistId();
        var secondPhysiotherapistId = new PhysiotherapistId();
        patient.assignPhysiotherapist(firstPhysiotherapistId, clinicId);

        patient.assignPhysiotherapist(secondPhysiotherapistId, clinicId);

        assertEquals(secondPhysiotherapistId, patient.getAssignedPhysiotherapistId());
        assertEquals(PatientStatus.IN_TREATMENT, patient.getStatus());
    }

    @Test
    void assignPhysiotherapistRejectsDischargedPatients() {
        var clinicId = new ClinicId();
        var patient = createPatient(clinicId);
        patient.assignPhysiotherapist(new PhysiotherapistId(), clinicId);
        patient.complete();
        patient.discharge();

        assertThrows(PatientOperationNotAllowedException.class, () -> patient.assignPhysiotherapist(new PhysiotherapistId(), clinicId));
    }

    private Patient createPatient(ClinicId clinicId) {
        return new Patient(
                new UserId(),
                clinicId,
                new FirstName("Lucia"),
                new LastName("Ramos"),
                new Dni("74839210"),
                new BirthDate(LocalDate.now().minusYears(30)),
                new Gender("FEMALE"),
                new Email("lucia.ramos@example.com"),
                new PhoneNumber("+51", "987654321"),
                new MedicalCondition("Post-operative knee rehabilitation")
        );
    }
}
