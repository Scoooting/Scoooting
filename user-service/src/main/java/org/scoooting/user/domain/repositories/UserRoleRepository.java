package org.scoooting.user.domain.repositories;

import org.scoooting.user.domain.model.UserRole;

import java.util.Optional;

public interface UserRoleRepository extends Repository<UserRole, Long> {
    Optional<UserRole> findByName(String name);
}
