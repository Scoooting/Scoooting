package org.scoooting.repositories;

import org.scoooting.entities.Scooter;
import org.springframework.data.domain.Page;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScooterRepository extends CrudRepository<Scooter, Long> {

    @Query("select sc.*, c.name from scooters as sc join cities as c on c.name = :city " +
            "where st_contains(" +
            "    st_makeenvelope(c.latitude_min, c.longitude_min, c.latitude_max, c.longitude_max, 4376)," +
            "    st_setsrid(st_makepoint(sc.latitude, sc.longitude), 4376)) offset :offset limit :limit;")
    List<Scooter> getScootersInCity(@Param("city") String city, @Param("offset") int offset, @Param("limit") int limit);

    @Query("select * from scooters where earth_distance(latitude, longitude, :lat, :lon) <= 2000 and status = 'FREE'")
    List<Scooter> getNearestScooters(@Param("lat") float lat, @Param("lon") float lon);

}
