package org.scoooting.rental.repositories;

import org.scoooting.rental.entities.Rental;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface RentalRepository extends CrudRepository<Rental, Long> {

    @Query("""
        SELECT * FROM rentals 
        WHERE user_id = :userId 
        AND status_id = (SELECT id FROM rental_statuses WHERE name = 'ACTIVE')
        """)
    Optional<Rental> findActiveRentalByUserId(Long userId);

    @Query("""
        SELECT * FROM rentals 
        WHERE user_id = :userId 
        ORDER BY start_time DESC
        LIMIT :limit OFFSET :offset
        """)
    List<Rental> findRentalHistoryByUserId(Long userId, int offset, int limit);

    @Query("SELECT COUNT(*) FROM rentals WHERE user_id = :userId")
    long countRentalsByUserId(Long userId);

    List<Rental> findByStatusId(Long statusId);
}