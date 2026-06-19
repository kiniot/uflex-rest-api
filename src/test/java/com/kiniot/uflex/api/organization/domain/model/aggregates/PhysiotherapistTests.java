package com.kiniot.uflex.api.organization.domain.model.aggregates;

import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistAlreadySuspendedException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistNotSuspendedException;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.LicenseNumber;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhoneNumber;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhysiotherapistStatus;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ProfessionalSummary;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.Specialty;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhysiotherapistTests {

    @Test
    void createPhysiotherapistStartsInactive() {
        var physiotherapist = createPhysiotherapist();

        assertEquals(PhysiotherapistStatus.INACTIVE, physiotherapist.getStatus());
    }

    @Test
    void synchronizeAvailabilityMarksPhysiotherapistActiveWhenHasAssignedPatientsInCharge() {
        var physiotherapist = createPhysiotherapist();

        physiotherapist.synchronizeAvailability(true);

        assertEquals(PhysiotherapistStatus.ACTIVE, physiotherapist.getStatus());
    }

    @Test
    void synchronizeAvailabilityMarksPhysiotherapistInactiveWhenHasNoAssignedPatientsInCharge() {
        var physiotherapist = createPhysiotherapist();
        physiotherapist.synchronizeAvailability(true);

        physiotherapist.synchronizeAvailability(false);

        assertEquals(PhysiotherapistStatus.INACTIVE, physiotherapist.getStatus());
    }

    @Test
    void suspendRejectedWhenPhysiotherapistAlreadySuspended() {
        var physiotherapist = createPhysiotherapist();
        physiotherapist.suspend();

        assertThrows(PhysiotherapistAlreadySuspendedException.class, physiotherapist::suspend);
    }

    @Test
    void reactivateRejectedWhenPhysiotherapistIsNotSuspended() {
        var physiotherapist = createPhysiotherapist();

        assertThrows(PhysiotherapistNotSuspendedException.class, () -> physiotherapist.reactivate(true));
    }

    @Test
    void reactivateSuspendedPhysiotherapistRecalculatesStatus() {
        var physiotherapist = createPhysiotherapist();
        physiotherapist.suspend();

        physiotherapist.reactivate(true);

        assertEquals(PhysiotherapistStatus.ACTIVE, physiotherapist.getStatus());
    }

    private Physiotherapist createPhysiotherapist() {
        return new Physiotherapist(
                new UserId(),
                new ClinicId(),
                "Pepito Perez",
                Specialty.NEUROLOGICAL,
                new Email("physio@example.com"),
                new PhoneNumber("+51", "987654321"),
                new LicenseNumber("CPT12345"),
                new ProfessionalSummary("Neuro rehab specialist"),
                java.util.UUID.randomUUID(),
                10
        );
    }
}
