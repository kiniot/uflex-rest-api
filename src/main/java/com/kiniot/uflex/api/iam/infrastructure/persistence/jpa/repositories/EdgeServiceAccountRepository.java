package com.kiniot.uflex.api.iam.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.iam.domain.model.aggregates.EdgeServiceAccount;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.EdgeServiceAccountId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EdgeServiceAccountRepository extends JpaRepository<EdgeServiceAccount, EdgeServiceAccountId> {

    Optional<EdgeServiceAccount> findByUserId(UserId userId);

    Optional<EdgeServiceAccount> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);
}
