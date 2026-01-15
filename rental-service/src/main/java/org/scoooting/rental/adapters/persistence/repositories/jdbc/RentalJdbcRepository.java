package org.scoooting.rental.adapters.persistence.repositories.jdbc;

import org.scoooting.rental.adapters.persistence.entities.RentalEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalJdbcRepository extends CrudRepository<RentalEntity, Long> {

    @Query("""
        SELECT * FROM rentals 
        WHERE user_id = :userId 
        AND status_id = (SELECT id FROM rental_statuses WHERE name = 'ACTIVE')
        """)
    Optional<RentalEntity> findActiveRentalByUserId(Long userId);

    @Query("""
        SELECT * FROM rentals 
        WHERE user_id = :userId 
        ORDER BY start_time DESC
        LIMIT :limit OFFSET :offset
        """)
    List<RentalEntity> findRentalHistoryByUserId(Long userId, int offset, int limit);

    @Query("SELECT COUNT(*) FROM rentals WHERE user_id = :userId")
    long countRentalsByUserId(Long userId);

    // Для Analyst - все аренды
    @Query("""
        SELECT * FROM rentals 
        ORDER BY start_time DESC
        LIMIT :limit OFFSET :offset
        """)
    List<RentalEntity> findAllRentals(int offset, int limit);

    @Query("SELECT COUNT(*) FROM rentals")
    long countAllRentals();

    List<RentalEntity> findByStatusId(Long statusId);
}