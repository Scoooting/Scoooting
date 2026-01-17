package org.scoooting.rental.domain.repositories;

import org.scoooting.rental.domain.model.Rental;

import java.util.List;
import java.util.Optional;

public interface RentalRepository extends Repository<Rental, Long> {

    Optional<Rental> findActiveRentalByUserId(Long userId);

    List<Rental> findRentalHistoryByUserId(Long userId, int offset, int limit);

    List<Rental> findAllRentals(int offset, int limit);

    long countAllRentals();

    long countRentalsByUserId(Long userId);

    List<Rental> findByStatusId(Long statusId);

}
