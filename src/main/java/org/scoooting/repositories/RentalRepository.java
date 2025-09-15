package org.scoooting.repositories;

import org.scoooting.entities.Rental;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface RentalRepository extends CrudRepository<Rental, Long> {

    // Find active rental for user
    @Query("SELECT * FROM rentals WHERE user_id = :userId AND status = 'ACTIVE'")
    Optional<Rental> findActiveRentalByUserId(@Param("userId") Long userId);

    // Find user's rental history with pagination
    @Query("SELECT * FROM rentals WHERE user_id = :userId ORDER BY start_time DESC LIMIT :limit OFFSET :offset")
    List<Rental> findRentalHistoryByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    // Count total rentals for user
    @Query("SELECT COUNT(*) FROM rentals WHERE user_id = :userId")
    Long countRentalsByUserId(@Param("userId") Long userId);

    // FIXED QUERY 1: Top users by rental count and total spent
    @Query("""
        SELECT u.id,
               u.email,
               u.name,
               u.bonuses,
               u.role,
               COUNT(r.id) as rental_count,
               COALESCE(SUM(r.total_cost), 0) as total_spent,
               COALESCE(AVG(r.duration_minutes), 0) as avg_duration
        FROM users u 
        LEFT JOIN rentals r ON u.id = r.user_id 
            AND r.start_time >= :startDate 
            AND r.start_time <= :endDate 
            AND r.status = 'COMPLETED'
        GROUP BY u.id, u.email, u.name, u.bonuses, u.role
        HAVING COUNT(r.id) >= :minRentals
        ORDER BY total_spent DESC, rental_count DESC
        LIMIT :limit
        """)
    List<Map<String, Object>> findTopUsersByUsageInPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minRentals") int minRentals,
            @Param("limit") int limit
    );

    // FIXED QUERY 2: Transport utilization (works with both scooters and transports)
    @Query("""
        SELECT t.id,
               t.model,
               t.latitude,
               t.longitude,
               t.type as transport_type,
               COUNT(r.id) as total_rentals,
               COALESCE(SUM(r.duration_minutes), 0) as total_minutes_used,
               COALESCE(SUM(r.total_cost), 0) as total_revenue,
               COALESCE(AVG(r.duration_minutes), 0) as avg_rental_duration,
               MAX(r.start_time) as last_rental_time,
               CASE 
                   WHEN COUNT(r.id) = 0 THEN 'UNUSED'
                   WHEN COUNT(r.id) < 5 THEN 'LOW_USAGE'
                   WHEN COUNT(r.id) < 20 THEN 'MEDIUM_USAGE'
                   ELSE 'HIGH_USAGE'
               END as usage_category
        FROM transports t
        LEFT JOIN rentals r ON t.id = r.transport_id 
            AND r.start_time >= :startDate 
            AND r.status = 'COMPLETED'
        WHERE ST_DWithin(
            ST_SetSRID(ST_MakePoint(t.longitude, t.latitude), 4326),
            ST_SetSRID(ST_MakePoint(:centerLon, :centerLat), 4326),
            :radiusMeters
        )
        GROUP BY t.id, t.model, t.latitude, t.longitude, t.type
        ORDER BY total_revenue DESC, total_rentals DESC
        """)
    List<Map<String, Object>> findTransportUtilizationInArea(
            @Param("centerLat") Double centerLat,
            @Param("centerLon") Double centerLon,
            @Param("radiusMeters") Double radiusMeters,
            @Param("startDate") LocalDateTime startDate
    );

    // SIMPLIFIED: Get high usage transports for maintenance
    @Query("""
        SELECT t.id,
               t.model,
               t.type,
               t.latitude,
               t.longitude,
               COUNT(r.id) as rental_count,
               COALESCE(SUM(r.duration_minutes), 0) as total_usage_minutes
        FROM transports t
        LEFT JOIN rentals r ON t.id = r.transport_id 
            AND r.start_time >= :sinceDate
            AND r.status = 'COMPLETED'
        GROUP BY t.id, t.model, t.type, t.latitude, t.longitude
        HAVING COUNT(r.id) > :maxRentals OR COALESCE(SUM(r.duration_minutes), 0) > :maxMinutes
        ORDER BY total_usage_minutes DESC
        """)
    List<Map<String, Object>> findTransportsNeedingMaintenance(
            @Param("sinceDate") LocalDateTime sinceDate,
            @Param("maxRentals") int maxRentals,
            @Param("maxMinutes") int maxMinutes
    );
}