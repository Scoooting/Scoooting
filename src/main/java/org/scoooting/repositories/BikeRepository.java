package org.scoooting.repositories;

import org.scoooting.entities.Bike;
import org.scoooting.entities.enums.BikeStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BikeRepository extends CrudRepository<Bike, Long> {

    @Query("SELECT * FROM bikes WHERE earth_distance(latitude, longitude, :lat, :lon) <= :radius AND status = 'FREE'")
    List<Bike> getNearestBikes(@Param("lat") float lat, @Param("lon") float lon, @Param("radius") int radius);

    @Query("SELECT * FROM bikes WHERE status = :status")
    List<Bike> findByStatus(@Param("status") BikeStatus status);

    @Query("SELECT COUNT(*) FROM bikes WHERE status = 'FREE'")
    Long countAvailable();
}