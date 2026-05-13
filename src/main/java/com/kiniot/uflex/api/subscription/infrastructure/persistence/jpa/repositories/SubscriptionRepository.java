package com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.subscription.domain.model.aggregates.Subscription;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionId> {
    @Query("select s from Subscription s where s.clinicId.clinicId = :clinicId")
    Optional<Subscription> findByClinicId(@Param("clinicId") UUID clinicId);

    @Query("select s from Subscription s where s.clinicId.clinicId = :clinicId and s.status = :status")
    Optional<Subscription> findByClinicIdAndStatus(@Param("clinicId") UUID clinicId, @Param("status") SubscriptionStatus status);

    @Query("""
            select s from Subscription s
            where s.clinicId.clinicId = :clinicId
              and exists (
                  select i from Invoice i
                  where i.subscriptionId = s.id.id
                    and i.status = com.kiniot.uflex.api.subscription.domain.model.valueobjects.InvoiceStatus.PAID
              )
            order by s.updatedAt desc
            """)
    List<Subscription> findPaidSubscriptionsByClinicId(@Param("clinicId") UUID clinicId);

    @Query("select s from Subscription s where s.paymentReference.providerCheckoutSessionId = :checkoutSessionId")
    Optional<Subscription> findByCheckoutSessionId(@Param("checkoutSessionId") String checkoutSessionId);

    @Query("select count(s) > 0 from Subscription s where s.clinicId.clinicId = :clinicId and s.status = :status")
    boolean existsByClinicIdAndStatus(@Param("clinicId") UUID clinicId, @Param("status") SubscriptionStatus status);
}
