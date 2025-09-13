package org.scoooting.repositories;

import org.scoooting.entities.Scooter;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScooterRepository extends CrudRepository<Scooter, Long> {

    @Query("select * from scooters where (SELECT st_distance(" +
            "ST_SetSRID(ST_MakePoint(latitude, longitude), 4326)::geography, " +
            "ST_SetSRID(ST_MakePoint(:userLatitude, :userLongitude), 4326)::geography)) <= 1000 " +
            "and status = 'FREE';")
    List<Scooter> getNearestScooters(@Param("userLatitude") double userLatitude,
                                     @Param("userLongitude") double userLongitude);
}
