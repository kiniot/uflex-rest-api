package com.kiniot.uflex.api.organization.application.internal.commandservices;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistLicenseInvalidException;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.LicenseNumber;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhoneNumber;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.PhotoUrl;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.ProfessionalSummary;
import com.kiniot.uflex.api.organization.domain.model.valueobjects.Specialty;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.PatientRepository;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.PhysiotherapistRepository;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PhysiotherapistCommandServiceImplTests {

    private PhysiotherapistRepository physiotherapistRepository;
    private ExternalIamService externalIamService;
    private PhysiotherapistCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        physiotherapistRepository = mock(PhysiotherapistRepository.class);
        var patientRepository = mock(PatientRepository.class);
        externalIamService = mock(ExternalIamService.class);
        var statusSynchronizationService = mock(PhysiotherapistStatusSynchronizationService.class);
        service = new PhysiotherapistCommandServiceImpl(
                physiotherapistRepository,
                patientRepository,
                externalIamService,
                statusSynchronizationService
        );
    }

    @Test
    void registerPhysiotherapistUsesAuthenticatedClinicAndPersistsProfile() {
        var command = createCommand();
        var userId = new UserId();
        var clinicId = new ClinicId(UUID.randomUUID());
        when(externalIamService.registerPhysiotherapist(command.emailAddress().email()))
                .thenReturn(Optional.of(userId));
        when(externalIamService.fetchCurrentClinicId()).thenReturn(Optional.of(clinicId));
        when(physiotherapistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(command).orElseThrow();

        assertEquals(userId, result.getUserId());
        assertEquals(clinicId, result.getClinicId());
        verify(physiotherapistRepository).save(result);
    }

    @Test
    void registerPhysiotherapistRejectsDuplicateLicenseWithinClinic() {
        var command = createCommand();
        var clinicId = new ClinicId(UUID.randomUUID());
        when(externalIamService.registerPhysiotherapist(command.emailAddress().email()))
                .thenReturn(Optional.of(new UserId()));
        when(externalIamService.fetchCurrentClinicId()).thenReturn(Optional.of(clinicId));
        when(physiotherapistRepository.existsByLicenseNumberAndClinicId(command.licenseNumber(), clinicId))
                .thenReturn(true);

        assertThrows(PhysiotherapistLicenseInvalidException.class, () -> service.handle(command));
    }

    private RegisterPhysiotherapistCommand createCommand() {
        return new RegisterPhysiotherapistCommand(
                "Pepito Perez",
                Specialty.NEUROLOGICAL,
                new Email("physio@example.com"),
                new PhoneNumber("+51", "987654321"),
                new LicenseNumber("CPT12345"),
                new ProfessionalSummary("Neurological rehabilitation specialist"),
                new PhotoUrl("https://example.com/physio.jpg"),
                10
        );
    }
}
