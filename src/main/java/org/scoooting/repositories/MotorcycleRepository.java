package org.scoooting.repositories;

import org.scoooting.entities.Motorcycle;
import org.scoooting.entities.enums.MotorcycleStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MotorcycleRepository extends CrudRepository<Motorcycle, Long> {

    @Query("SELECT * FROM motorcycles WHERE earth_distance(latitude, longitude, :lat, :lon) <= :radius AND status = 'FREE'")
    List<Motorcycle> getNearestMotorcycles(@Param("lat") float lat, @Param("lon") float lon, @Param("radius") int radius);

    @Query("SELECT * FROM motorcycles WHERE status = :status")
    List<Motorcycle> findByStatus(@Param("status") MotorcycleStatus status);

    @Query("SELECT COUNT(*) FROM motorcycles WHERE status = 'FREE'")
    Long countAvailable();
}