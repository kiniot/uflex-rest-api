package com.kiniot.uflex.api.iam.infrastructure.persistence.jpa.repositories;

import com.kiniot.uflex.api.iam.domain.model.entities.Role;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.RoleId;
import com.kiniot.uflex.api.iam.domain.model.valueobjects.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, RoleId> {
    Optional<Role> findByName(RoleName name);

    List<Role> findAllByNameIn(Collection<RoleName> names);

    boolean existsByName(RoleName name);
}