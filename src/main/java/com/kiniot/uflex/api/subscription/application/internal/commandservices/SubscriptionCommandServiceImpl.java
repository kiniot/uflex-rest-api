package com.kiniot.uflex.api.subscription.application.internal.commandservices;

import com.kiniot.uflex.api.shared.domain.exceptions.AuthenticatedUserClinicNotFoundException;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.acl.ExternalIamService;
import com.kiniot.uflex.api.subscription.domain.model.commands.CreateCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.PaymentGatewayPort;
import com.kiniot.uflex.api.subscription.domain.exceptions.CurrentSubscriptionAlreadyExistsException;
import com.kiniot.uflex.api.subscription.domain.exceptions.SubscriptionPriceMismatchException;
import com.kiniot.uflex.api.subscription.domain.exceptions.TierNotFoundException;
import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.domain.model.commands.CreateSubscriptionCommand;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionKitSelection;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionCheckoutResult;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionCommandService;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.TierRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class SubscriptionCommandServiceImpl implements SubscriptionCommandService {

    private final SubscriptionRepository subscriptionRepository;
    private final TierRepository tierRepository;
    private final ExternalIamService externalIamService;
    private final PaymentGatewayPort paymentGatewayPort;

    public SubscriptionCommandServiceImpl(
            SubscriptionRepository subscriptionRepository,
            ExternalIamService externalIamService,
            TierRepository tierRepository,
            PaymentGatewayPort paymentGatewayPort
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.externalIamService = externalIamService;
        this.tierRepository = tierRepository;
        this.paymentGatewayPort = paymentGatewayPort;
    }

    @Override
    @Transactional
    public Optional<SubscriptionCheckoutResult> handle(CreateSubscriptionCommand command) {
        var clinicId = externalIamService.fetchCurrentClinicId()
                .orElseThrow(AuthenticatedUserClinicNotFoundException::new);
        var hasCurrentSubscription = subscriptionRepository.findAllByClinicId(clinicId).stream()
                .anyMatch(subscription -> subscription.blocksNewSubscriptionAt(LocalDate.now()));
        if (hasCurrentSubscription) throw new CurrentSubscriptionAlreadyExistsException();
        var tier = tierRepository.findWithPricesById(command.selection().tierId())
                .orElseThrow(() -> new TierNotFoundException(command.selection().tierId().id().toString()));
        var catalogPrice = tier.getPrice(command.selection().billingPeriod(), command.contractedPrice().currency());
        if (!tier.isAllowsPriceOverride() && !catalogPrice.equals(command.contractedPrice()))
            throw new SubscriptionPriceMismatchException();
        var kitPricing = tier.getTierKitPricing();
        var requestedTotalKits = command.requestedTotalKits();
        var additionalKits = kitPricing.calculateAdditionalKits(requestedTotalKits);
        var unitKitPrice = kitPricing.resolveUnitPrice(command.contractedPrice().currency());
        var totalKitCharge = kitPricing.calculateTotalCharge(requestedTotalKits, command.contractedPrice().currency());
        var kitSelection = new SubscriptionKitSelection(
                requestedTotalKits,
                kitPricing.baseKits(),
                additionalKits,
                unitKitPrice,
                totalKitCharge
        );
        var checkoutAmount = command.contractedPrice().add(totalKitCharge);
        var subscription = subscriptionRepository.save(new Subscription(command, clinicId, kitSelection));
        var checkoutSession = paymentGatewayPort.createCheckoutSession(new CreateCheckoutSessionCommand(
                subscription.getId(),
                clinicId,
                subscription.getSelection(),
                tier.getName().name(),
                checkoutAmount,
                subscription.getContractedPrice(),
                subscription.getKitSelection()
        ));
        subscription.attachCheckoutSession(checkoutSession.sessionId());
        subscriptionRepository.save(subscription);
        return Optional.of(new SubscriptionCheckoutResult(
                subscription.getId(),
                subscription.getStatus(),
                checkoutSession.checkoutUrl()
        ));
    }
}
