package org.scoooting.user.adapters.persistence.repositories.jdbc;


import org.scoooting.user.adapters.persistence.entities.UserRoleEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleJdbcRepository extends CrudRepository<UserRoleEntity, Long> {
    Optional<UserRoleEntity> findByName(String name);
}