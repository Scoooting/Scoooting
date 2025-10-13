package org.scoooting.rental.repositories;

import org.scoooting.rental.entities.RentalStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RentalStatusRepository extends CrudRepository<RentalStatus, Long> {
    Optional<RentalStatus> findByName(String name);
}
