package com.kiniot.uflex.api.subscription.application.internal.commandservices;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CheckoutSessionResult;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.CreateCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.PaymentGatewayPort;
import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.domain.model.commands.CancelSubscriptionCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.ChangeSubscriptionPlanCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.CompleteCheckoutSessionPaymentCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.ConfirmCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.CreateSubscriptionCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.PurchaseSubscriptionPlanCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.RegisterInvoicePaymentCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.UpdatePaymentMethodCommand;
import com.kiniot.uflex.api.subscription.domain.model.entities.Invoice;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.InvoiceId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionPlanId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionStatus;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionCommandService;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionPricingService;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.InvoiceRepository;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.PlanRepository;
import com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class SubscriptionCommandServiceImpl implements SubscriptionCommandService {
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionPricingService pricingService;
    private final PaymentGatewayPort paymentGateway;

    public SubscriptionCommandServiceImpl(SubscriptionRepository subscriptionRepository, PlanRepository planRepository,
                                          InvoiceRepository invoiceRepository, SubscriptionPricingService pricingService,
                                          PaymentGatewayPort paymentGateway) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.invoiceRepository = invoiceRepository;
        this.pricingService = pricingService;
        this.paymentGateway = paymentGateway;
    }

    @Override
    @Transactional(readOnly = true)
    public CheckoutSessionResult handle(CreateSubscriptionCheckoutSessionCommand command) {
        if (subscriptionRepository.existsByClinicIdAndStatus(command.clinicId(), SubscriptionStatus.ACTIVE)) {
            throw new IllegalStateException("Clinic already has an active subscription");
        }
        var plan = planRepository.findById(new SubscriptionPlanId(command.planId()))
                .filter(com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Active plan not found"));
        var amount = pricingService.priceFor(plan, command.billingCycle());
        return paymentGateway.createCheckoutSession(new CreateCheckoutSessionCommand(
                command.clinicId(),
                command.planId(),
                command.billingCycle(),
                amount.amount(),
                amount.currency(),
                null,
                null,
                plan.getName(),
                command.userId()
        ));
    }

    @Override
    @Transactional
    public Optional<Subscription> handle(ConfirmCheckoutSessionCommand command) {
        if (command.sessionId() == null || command.sessionId().isBlank()) {
            throw new IllegalArgumentException("sessionId is required");
        }
        var existingBySession = subscriptionRepository.findByPaymentReferenceProviderCheckoutSessionId(command.sessionId());
        if (existingBySession.isPresent()) return existingBySession;

        var completedPayment = paymentGateway.confirmCheckoutSession(command.sessionId())
                .orElseThrow(() -> new IllegalArgumentException("Stripe Checkout Session was not found"));
        if (!"paid".equalsIgnoreCase(completedPayment.paymentStatus())) {
            throw new IllegalStateException("Stripe Checkout Session has not been paid");
        }
        if (!"complete".equalsIgnoreCase(completedPayment.status())) {
            throw new IllegalStateException("Stripe Checkout Session is not complete");
        }
        return handle(new CompleteCheckoutSessionPaymentCommand(
                completedPayment.clinicId(),
                completedPayment.planId(),
                completedPayment.billingCycle(),
                completedPayment.paymentReference(),
                completedPayment.currentPeriodStart(),
                completedPayment.currentPeriodEnd()
        ));
    }

    @Override
    @Transactional
    public Optional<Subscription> handle(CompleteCheckoutSessionPaymentCommand command) {
        if (command.paymentReference() != null && command.paymentReference().providerCheckoutSessionId() != null) {
            var existingBySession = subscriptionRepository.findByPaymentReferenceProviderCheckoutSessionId(
                    command.paymentReference().providerCheckoutSessionId());
            if (existingBySession.isPresent()) return existingBySession;
        }
        var plan = planRepository.findById(new SubscriptionPlanId(command.planId()))
                .filter(com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Active plan not found"));
        var amount = pricingService.priceFor(plan, command.billingCycle());
        var now = command.currentPeriodStart() == null ? OffsetDateTime.now() : command.currentPeriodStart();
        var periodEnd = command.currentPeriodEnd() == null
                ? (command.billingCycle() == com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle.YEARLY ? now.plusYears(1) : now.plusMonths(1))
                : command.currentPeriodEnd();
        var subscription = subscriptionRepository.findByClinicIdAndStatus(command.clinicId(), SubscriptionStatus.ACTIVE)
                .orElseGet(() -> new Subscription(new ClinicId(command.clinicId()), plan, command.billingCycle(), command.paymentReference()));
        subscription.refreshStripePayment(command.paymentReference(), now, periodEnd);
        var savedSubscription = subscriptionRepository.save(subscription);
        savePaidInvoiceIfAbsent(savedSubscription, amount, command.paymentReference().providerTransactionId(), now);
        return Optional.of(savedSubscription);
    }

    @Override
    @Transactional
    public Optional<Subscription> handle(PurchaseSubscriptionPlanCommand command) {
        if (subscriptionRepository.existsByClinicIdAndStatus(command.clinicId(), SubscriptionStatus.ACTIVE)) {
            throw new IllegalStateException("Clinic already has an active subscription");
        }
        var plan = planRepository.findById(new SubscriptionPlanId(command.planId()))
                .filter(com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Active plan not found"));
        var amount = pricingService.priceFor(plan, command.billingCycle());
        var paymentReference = paymentGateway.charge(amount, command.paymentToken());
        var subscription = new Subscription(new ClinicId(command.clinicId()), plan, command.billingCycle(), paymentReference);
        var now = OffsetDateTime.now();
        subscription.activate(now);
        var invoice = new Invoice(subscription.getId().id(), amount, now, now);
        invoice.markPaid(paymentReference.providerTransactionId(), now);
        var savedSubscription = subscriptionRepository.save(subscription);
        invoiceRepository.save(invoice);
        return Optional.of(savedSubscription);
    }

    @Override
    @Transactional
    public Optional<Subscription> handle(ChangeSubscriptionPlanCommand command) {
        var subscription = subscriptionRepository.findById(new SubscriptionId(command.subscriptionId()))
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        var plan = planRepository.findById(new SubscriptionPlanId(command.newPlanId()))
                .filter(com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Active plan not found"));
        var amount = pricingService.amountForPlanChange(plan, command.newBillingCycle());
        var paymentReference = paymentGateway.charge(amount, "mock-plan-change");
        var changedAt = command.effectiveAt() == null ? OffsetDateTime.now() : command.effectiveAt();
        subscription.changePlan(plan, command.newBillingCycle(), paymentReference, changedAt);
        var invoice = new Invoice(subscription.getId().id(), amount, changedAt, changedAt);
        invoice.markPaid(paymentReference.providerTransactionId(), changedAt);
        invoiceRepository.save(invoice);
        return Optional.of(subscriptionRepository.save(subscription));
    }

    @Override
    @Transactional
    public Optional<Subscription> handle(CancelSubscriptionCommand command) {
        var subscription = subscriptionRepository.findById(new SubscriptionId(command.subscriptionId()))
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        subscription.cancel();
        return Optional.of(subscriptionRepository.save(subscription));
    }

    @Override
    @Transactional
    public Optional<Subscription> handle(UpdatePaymentMethodCommand command) {
        var subscription = subscriptionRepository.findById(new SubscriptionId(command.subscriptionId()))
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        var paymentReference = paymentGateway.updatePaymentMethod(command.paymentToken());
        subscription.updatePaymentReference(paymentReference);
        return Optional.of(subscriptionRepository.save(subscription));
    }

    @Override
    @Transactional
    public void handle(RegisterInvoicePaymentCommand command) {
        invoiceRepository.findById(new InvoiceId(command.invoiceId())).ifPresent(invoice -> {
            if (command.successful()) {
                invoice.markPaid(command.providerTransactionId(), OffsetDateTime.now());
            } else {
                invoice.markFailed(command.providerTransactionId());
            }
            invoiceRepository.save(invoice);
        });
    }

    private void savePaidInvoiceIfAbsent(Subscription subscription, Money amount, String providerTransactionId, OffsetDateTime paidAt) {
        if (providerTransactionId != null && invoiceRepository.findByProviderTransactionId(providerTransactionId).isPresent()) {
            return;
        }
        var invoice = new Invoice(subscription.getId().id(), amount, paidAt, paidAt);
        invoice.markPaid(providerTransactionId, paidAt);
        invoiceRepository.save(invoice);
    }
}
