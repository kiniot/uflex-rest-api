package com.kiniot.uflex.api.subscription.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.subscription.domain.model.entities.Tier;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.TierId;
import com.kiniot.uflex.api.subscription.domain.model.valueobjects.TierName;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface TierRepository extends JpaRepository<Tier, TierId> {
    boolean existsByName(TierName name);
    Optional<Tier> findByName(TierName name);

    @EntityGraph(attributePaths = "prices")
    List<Tier> findAllWithPricesBy();

    @EntityGraph(attributePaths = "prices")
    Optional<Tier> findWithPricesById(TierId tierId);

    @EntityGraph(attributePaths = "prices")
    Optional<Tier> findWithPricesByName(TierName name);
}
