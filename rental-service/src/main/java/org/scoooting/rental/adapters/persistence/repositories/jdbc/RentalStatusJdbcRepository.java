package org.scoooting.rental.adapters.persistence.repositories.jdbc;

import org.scoooting.rental.adapters.persistence.entities.RentalStatusEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RentalStatusJdbcRepository extends CrudRepository<RentalStatusEntity, Long> {
    Optional<RentalStatusEntity> findByName(String name);
}
