package com.kiniot.uflex.api.organization.application.internal.commandservices;

import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.organization.application.internal.outboundservices.acl.ExternalMediaService;
import com.kiniot.uflex.api.organization.domain.exceptions.ClinicNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistAlreadyRegisteredException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistClinicMismatchException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistHasAssignedPatientsException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistPhotoAssetInvalidException;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Patient;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistLicenseInvalidException;
import com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistNotFoundException;
import com.kiniot.uflex.api.organization.domain.exceptions.UserProvisioningException;
import com.kiniot.uflex.api.organization.domain.model.aggregates.Physiotherapist;
import com.kiniot.uflex.api.organization.domain.model.commands.DeletePhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.ReactivatePhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.RegisterPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.SuspendPhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.model.commands.UpdatePhysiotherapistCommand;
import com.kiniot.uflex.api.organization.domain.services.PhysiotherapistCommandService;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.PatientRepository;
import com.kiniot.uflex.api.organization.infrastructure.persistence.jpa.repositories.PhysiotherapistRepository;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class PhysiotherapistCommandServiceImpl implements PhysiotherapistCommandService {

    private final PhysiotherapistRepository physiotherapistRepository;
    private final PatientRepository patientRepository;
    private final ExternalIamService externalIamService;
    private final ExternalMediaService externalMediaService;
    private final PhysiotherapistStatusSynchronizationService physiotherapistStatusSynchronizationService;

    public PhysiotherapistCommandServiceImpl(
            PhysiotherapistRepository physiotherapistRepository,
            PatientRepository patientRepository,
            ExternalIamService externalIamService,
            ExternalMediaService externalMediaService,
            PhysiotherapistStatusSynchronizationService physiotherapistStatusSynchronizationService
    ) {
        this.physiotherapistRepository = physiotherapistRepository;
        this.patientRepository = patientRepository;
        this.externalIamService = externalIamService;
        this.externalMediaService = externalMediaService;
        this.physiotherapistStatusSynchronizationService = physiotherapistStatusSynchronizationService;
    }

    @Override
    @Transactional
    public Optional<Physiotherapist> handle(RegisterPhysiotherapistCommand command) {
        var userId = externalIamService.registerPhysiotherapist(command.emailAddress().email())
                .orElseThrow(() -> new UserProvisioningException("Failed to register physiotherapist in IAM"));
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new ClinicNotFoundException("Current clinic not found"));

        if (physiotherapistRepository.existsByLicenseNumberAndClinicId(command.licenseNumber(), clinicId)) {
            throw new PhysiotherapistLicenseInvalidException(command.licenseNumber().licenseNumber());
        }
        if (physiotherapistRepository.existsByEmailAddressAndClinicId(command.emailAddress(), clinicId)) {
            throw new com.kiniot.uflex.api.organization.domain.exceptions.PhysiotherapistAlreadyRegisteredException(
                    "Physiotherapist with email " + command.emailAddress().email() + " already registered for this clinic");
        }
        validatePhotoAsset(command.photoAssetId(), clinicId.id().toString());
        var physiotherapist = new Physiotherapist(command, userId, clinicId);
        if (command.photoAssetId() != null) {
            externalMediaService.assignProfilePhoto(command.photoAssetId(), physiotherapist.getId().physiotherapistId());
        }
        physiotherapist.register();
        return Optional.of(physiotherapistRepository.save(physiotherapist));
    }

    @Override
    @Transactional
    public Optional<Physiotherapist> handle(UpdatePhysiotherapistCommand command) {
        var physiotherapist = getPhysiotherapistInCurrentClinic(command.physiotherapistId());
        validatePhysiotherapistUpdate(physiotherapist, command);
        var previousEmail = physiotherapist.getEmailAddress();
        boolean photoIsChanging = !Objects.equals(command.photoAssetId(), physiotherapist.getPhotoAssetId());
        if (photoIsChanging && command.photoAssetId() != null) {
            validatePhotoAsset(command.photoAssetId(), physiotherapist.getClinicId().id().toString());
        }
        physiotherapist.updateProfile(
                command.fullName(),
                command.specialty(),
                command.emailAddress(),
                command.phoneNumber(),
                command.licenseNumber(),
                command.professionalSummary(),
                command.photoAssetId(),
                command.yearsOfExperience()
        );
        if (photoIsChanging && command.photoAssetId() != null) {
            externalMediaService.assignProfilePhoto(command.photoAssetId(), physiotherapist.getId().physiotherapistId());
        }
        var savedPhysiotherapist = physiotherapistRepository.save(physiotherapist);
        if (!previousEmail.equals(command.emailAddress())) {
            externalIamService.updateUserEmail(savedPhysiotherapist.getUserId(), command.emailAddress().email());
        }
        return Optional.of(savedPhysiotherapist);
    }

    @Override
    @Transactional
    public void handle(SuspendPhysiotherapistCommand command) {
        var physiotherapist = getPhysiotherapistInCurrentClinic(command.physiotherapistId());
        physiotherapist.suspend();
        var assignedPatients = patientRepository.findAllByAssignedPhysiotherapistIdAndClinicId(
                physiotherapist.getId(),
                physiotherapist.getClinicId()
        );
        assignedPatients.forEach(Patient::unassignPhysiotherapist);
        patientRepository.saveAll(assignedPatients);
        physiotherapistRepository.save(physiotherapist);
    }

    @Override
    @Transactional
    public void handle(ReactivatePhysiotherapistCommand command) {
        var physiotherapist = getPhysiotherapistInCurrentClinic(command.physiotherapistId());
        physiotherapistStatusSynchronizationService.reactivate(physiotherapist);
    }

    @Override
    @Transactional
    public void handle(DeletePhysiotherapistCommand command) {
        var physiotherapist = getPhysiotherapistInCurrentClinic(command.physiotherapistId());
        if (patientRepository.existsByAssignedPhysiotherapistId(physiotherapist.getId())) {
            throw new PhysiotherapistHasAssignedPatientsException(physiotherapist.getId().physiotherapistId().toString());
        }
        var userId = physiotherapist.getUserId();
        physiotherapistRepository.delete(physiotherapist);
        externalIamService.deleteUserById(userId);
    }

    private Physiotherapist getPhysiotherapistInCurrentClinic(com.kiniot.uflex.api.shared.domain.model.valueobjects.PhysiotherapistId physiotherapistId) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(() -> new ClinicNotFoundException("Current clinic not found"));
        var physiotherapist = physiotherapistRepository.findById(physiotherapistId)
                .orElseThrow(() -> new PhysiotherapistNotFoundException("Physiotherapist not found"));
        validatePhysiotherapistBelongsToClinic(physiotherapist, clinicId);
        return physiotherapist;
    }

    private void validatePhysiotherapistBelongsToClinic(Physiotherapist physiotherapist, ClinicId clinicId) {
        if (!physiotherapist.getClinicId().equals(clinicId)) {
            throw new PhysiotherapistClinicMismatchException();
        }
    }

    private void validatePhysiotherapistUpdate(Physiotherapist physiotherapist, UpdatePhysiotherapistCommand command) {
        var clinicId = physiotherapist.getClinicId();
        if (!physiotherapist.getLicenseNumber().equals(command.licenseNumber())
                && physiotherapistRepository.existsByLicenseNumberAndClinicId(command.licenseNumber(), clinicId)) {
            throw new PhysiotherapistLicenseInvalidException(command.licenseNumber().licenseNumber());
        }
        if (!physiotherapist.getEmailAddress().equals(command.emailAddress())
                && physiotherapistRepository.existsByEmailAddressAndClinicId(command.emailAddress(), clinicId)) {
            throw new PhysiotherapistAlreadyRegisteredException(
                    "Physiotherapist with email " + command.emailAddress().email() + " already registered for this clinic");
        }
    }

    private void validatePhotoAsset(java.util.UUID photoAssetId, String clinicId) {
        if (photoAssetId == null) {
            return;
        }
        var asset = externalMediaService.findMediaAssetById(photoAssetId)
                .orElseThrow(() -> new PhysiotherapistPhotoAssetInvalidException(
                        "Photo asset not found: " + photoAssetId));
        if (!clinicId.equals(asset.clinicId())) {
            throw new PhysiotherapistPhotoAssetInvalidException(
                    "Photo asset does not belong to the authenticated clinic");
        }
        if (!"UPLOADED".equals(asset.status())) {
            throw new PhysiotherapistPhotoAssetInvalidException(
                    "Photo asset must be in UPLOADED status");
        }
        if (!"IMAGE".equals(asset.mediaType())) {
            throw new PhysiotherapistPhotoAssetInvalidException(
                    "Photo asset must be an IMAGE");
        }
    }
}
