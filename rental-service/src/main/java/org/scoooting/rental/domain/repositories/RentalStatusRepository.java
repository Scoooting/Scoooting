package org.scoooting.rental.domain.repositories;

import org.scoooting.rental.domain.model.RentalStatus;

import java.util.Optional;

public interface RentalStatusRepository extends Repository<RentalStatus, Long> {
    Optional<RentalStatus> findByName(String name);
}
