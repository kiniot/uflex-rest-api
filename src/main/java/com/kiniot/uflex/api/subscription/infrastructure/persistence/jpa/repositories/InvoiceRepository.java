package com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.subscription.domain.model.entities.Invoice;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.InvoiceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, InvoiceId> {
    List<Invoice> findBySubscriptionId(UUID subscriptionId);
    Optional<Invoice> findByProviderTransactionId(String providerTransactionId);
}
