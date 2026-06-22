package com.kiniot.uflex.api.iam.application.internal.commandservices;

import com.kiniot.uflex.api.iam.application.internal.outboundservices.hashing.HashingService;
import com.kiniot.uflex.api.iam.application.internal.outboundservices.identity.IdentityService;
import com.kiniot.uflex.api.iam.application.internal.outboundservices.verification.VerificationService;
import com.kiniot.uflex.api.iam.domain.exceptions.EdgeServiceAccountAlreadyExistsException;
import com.kiniot.uflex.api.iam.domain.exceptions.EmailAlreadyInUseException;
import com.kiniot.uflex.api.iam.domain.exceptions.RoleNotFoundException;
import com.kiniot.uflex.api.iam.domain.model.aggregates.EdgeServiceAccount;
import com.kiniot.uflex.api.iam.domain.model.aggregates.User;
import com.kiniot.uflex.api.iam.domain.model.commands.ProvisionEdgeServiceAccountCommand;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.EdgeServiceAccountCredentials;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Email;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.Password;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.RoleName;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.TenantId;
import com.kiniot.uflex.api.iam.domain.services.EdgeServiceAccountCommandService;
import com.kiniot.uflex.api.iam.infrastructure.persistence.jpa.repositories.EdgeServiceAccountRepository;
import com.kiniot.uflex.api.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.kiniot.uflex.api.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedTenantNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EdgeServiceAccountCommandServiceImpl implements EdgeServiceAccountCommandService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EdgeServiceAccountRepository edgeServiceAccountRepository;
    private final HashingService hashingService;
    private final IdentityService identityService;
    private final VerificationService verificationService;

    public EdgeServiceAccountCommandServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            EdgeServiceAccountRepository edgeServiceAccountRepository,
            HashingService hashingService,
            IdentityService identityService,
            VerificationService verificationService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.edgeServiceAccountRepository = edgeServiceAccountRepository;
        this.hashingService = hashingService;
        this.identityService = identityService;
        this.verificationService = verificationService;
    }

    @Override
    @Transactional
    public EdgeServiceAccountCredentials handle(ProvisionEdgeServiceAccountCommand command) {
        var serialNumber = command.serialNumber();
        if (edgeServiceAccountRepository.existsBySerialNumber(serialNumber))
            throw new EdgeServiceAccountAlreadyExistsException(serialNumber);

        var tenantId = resolveOwningClinic(command.clinicId());

        var emailAddress = "edge-" + serialNumber.toLowerCase() + "@uflex.local";
        var email = new Email(emailAddress);
        if (userRepository.existsByEmail(email))
            throw new EmailAlreadyInUseException(emailAddress);

        var password = verificationService.generateRandomPassword();
        var edgeRole = roleRepository.findByName(RoleName.ROLE_EDGE)
                .orElseThrow(() -> new RoleNotFoundException(RoleName.ROLE_EDGE.name()));

        // Service account: created already verified, with no welcome/activation email.
        var user = new User(email, new Password(hashingService.encode(password)), List.of(edgeRole), tenantId);
        userRepository.save(user);

        var account = new EdgeServiceAccount(user.getId(), serialNumber, tenantId);
        edgeServiceAccountRepository.save(account);

        return new EdgeServiceAccountCredentials(emailAddress, password, serialNumber);
    }

    /**
     * Resolves the owning clinic for the new edge account.
     * <p>
     * A {@code ROLE_DEVELOPER} (platform/ops, no tenant context) must supply {@code clinicId}
     * explicitly. A {@code ROLE_CLINIC_ADMIN} provisions for their own clinic: the body
     * {@code clinicId} is optional and, if present, must match their own clinic.
     */
    private TenantId resolveOwningClinic(String requestedClinicId) {
        if (identityService.getRoles().contains(RoleName.ROLE_DEVELOPER.name())) {
            if (requestedClinicId == null || requestedClinicId.isBlank())
                throw new IllegalArgumentException("clinicId is required when provisioning as a developer");
            return new TenantId(UUID.fromString(requestedClinicId));
        }
        var contextClinicId = identityService.getTenantId()
                .orElseThrow(AuthenticatedTenantNotFoundException::new);
        if (requestedClinicId != null && !requestedClinicId.isBlank()
                && !requestedClinicId.equals(contextClinicId)) {
            throw new AccessDeniedException("A clinic admin can only provision edges for their own clinic");
        }
        return new TenantId(UUID.fromString(contextClinicId));
    }
}
