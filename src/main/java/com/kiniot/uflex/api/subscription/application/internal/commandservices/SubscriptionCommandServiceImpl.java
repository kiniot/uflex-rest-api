package com.kiniot.uflex.api.subscription.application.internal.commandservices;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.subscription.application.internal.outboundservices.payment.SubscriptionPaymentService;
import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.domain.model.commands.CancelSubscriptionCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.ChangeSubscriptionPlanCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.CompleteCheckoutSessionPaymentCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.ConfirmCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.CreateChangePlanCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.CreateSubscriptionCheckoutSessionCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.PurchaseSubscriptionPlanCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.RegisterInvoicePaymentCommand;
import com.kiniot.uflex.api.subscription.domain.model.commands.UpdatePaymentMethodCommand;
import com.kiniot.uflex.api.subscription.domain.model.entities.Invoice;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.InvoiceId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.Money;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionStatus;
import com.kiniot.uflex.api.subscription.domain.services.SubscriptionCommandService;
import com.kiniot.uflex.api.subscription.domain.services.results.CheckoutSessionResult;
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
    private final SubscriptionPaymentService paymentGateway;

    public SubscriptionCommandServiceImpl(SubscriptionRepository subscriptionRepository, PlanRepository planRepository,
                                          InvoiceRepository invoiceRepository,
                                          SubscriptionPaymentService paymentGateway) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentGateway = paymentGateway;
    }

    @Override
    @Transactional(readOnly = true)
    public CheckoutSessionResult handle(CreateSubscriptionCheckoutSessionCommand command) {
        if (subscriptionRepository.existsByClinicIdAndStatus(command.clinicId().id(), SubscriptionStatus.ACTIVE)) {
            throw new IllegalStateException("Clinic already has an active subscription");
        }
        var plan = planRepository.findById(command.planId())
                .filter(com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Active plan not found"));
        var amount = plan.priceFor(command.billingCycle());
        return paymentGateway.createCheckoutSession(
                command.clinicId().id(),
                command.planId().id(),
                command.billingCycle(),
                amount,
                null,
                null,
                plan.getName(),
                command.userId() == null ? null : command.userId().id().toString()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CheckoutSessionResult handle(CreateChangePlanCheckoutSessionCommand command) {
        var subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Only active subscriptions can change plans");
        }
        var plan = planRepository.findById(command.newPlanId())
                .filter(com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Active plan not found"));
        var amount = plan.amountForPlanChange(command.newBillingCycle());
        return paymentGateway.createCheckoutSession(
                subscription.getClinicId().id(),
                command.newPlanId().id(),
                command.newBillingCycle(),
                amount,
                null,
                null,
                plan.getName(),
                command.userId() == null ? null : command.userId().id().toString()
        );
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
                new ClinicId(completedPayment.clinicId()),
                new com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionPlanId(completedPayment.planId()),
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
        var plan = planRepository.findById(command.planId())
                .filter(com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Active plan not found"));
        var amount = plan.priceFor(command.billingCycle());
        var now = command.currentPeriodStart() == null ? OffsetDateTime.now() : command.currentPeriodStart();
        var periodEnd = command.currentPeriodEnd() == null
                ? (command.billingCycle() == com.kiniot.uflex.api.subscription.domain.model.valueobjects.BillingCycle.YEARLY ? now.plusYears(1) : now.plusMonths(1))
                : command.currentPeriodEnd();
        var subscription = subscriptionRepository.findByClinicIdAndStatus(command.clinicId().id(), SubscriptionStatus.ACTIVE)
                .orElseGet(() -> new Subscription(command.clinicId(), plan, command.billingCycle(), command.paymentReference()));
        subscription.refreshStripePayment(command.paymentReference(), now, periodEnd);
        var savedSubscription = subscriptionRepository.save(subscription);
        savePaidInvoiceIfAbsent(savedSubscription, amount, command.paymentReference().providerTransactionId(), now);
        return Optional.of(savedSubscription);
    }

    @Override
    @Transactional
    public Optional<Subscription> handle(PurchaseSubscriptionPlanCommand command) {
        if (subscriptionRepository.existsByClinicIdAndStatus(command.clinicId().id(), SubscriptionStatus.ACTIVE)) {
            throw new IllegalStateException("Clinic already has an active subscription");
        }
        var plan = planRepository.findById(command.planId())
                .filter(com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Active plan not found"));
        var amount = plan.priceFor(command.billingCycle());
        var paymentReference = paymentGateway.charge(amount, command.paymentToken());
        var subscription = new Subscription(command.clinicId(), plan, command.billingCycle(), paymentReference);
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
        var subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        var plan = planRepository.findById(command.newPlanId())
                .filter(com.kiniot.uflex.api.subscription.domain.model.entities.SubscriptionPlan::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Active plan not found"));
        var amount = plan.amountForPlanChange(command.newBillingCycle());
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
        var subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        subscription.cancel();
        return Optional.of(subscriptionRepository.save(subscription));
    }

    @Override
    @Transactional
    public Optional<Subscription> handle(UpdatePaymentMethodCommand command) {
        var subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        var paymentReference = paymentGateway.updatePaymentMethod(command.paymentToken());
        subscription.updatePaymentReference(paymentReference);
        return Optional.of(subscriptionRepository.save(subscription));
    }

    @Override
    @Transactional
    public void handle(RegisterInvoicePaymentCommand command) {
        invoiceRepository.findById(command.invoiceId()).ifPresent(invoice -> {
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
