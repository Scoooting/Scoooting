package org.scoooting.repositories;

import org.scoooting.entities.Transport;
import org.scoooting.entities.enums.TransportStatus;
import org.scoooting.entities.enums.TransportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportRepository extends CrudRepository<Transport, Long> {

    @Query("select t.*, c.name from transports as t join cities as c on c.name = :city " +
            "where st_contains(" +
            "    st_makeenvelope(c.latitude_min, c.longitude_min, c.latitude_max, c.longitude_max, 4376)," +
            "    st_setsrid(st_makepoint(t.latitude, t.longitude), 4376)) offset :offset limit :limit;")
    List<Transport> getTransportsInCity(@Param("city") String city, @Param("offset") int offset, @Param("limit") int limit);

    @Query("select * from transports where earth_distance(latitude, longitude, :lat, :lon) <= 2000 and status = 'FREE'")
    List<Transport> getNearestTransports(@Param("lat") float lat, @Param("lon") float lon);

    @Query("select * from transports where earth_distance(latitude, longitude, :lat, :lon) <= :radius and status = 'FREE' and type = :type")
    List<Transport> getNearestTransportsByType(@Param("lat") float lat, @Param("lon") float lon, @Param("radius") int radius, @Param("type") TransportType type);

    @Query("select * from transports where type = :type and status = :status")
    List<Transport> findByTypeAndStatus(@Param("type") TransportType type, @Param("status") TransportStatus status);

    @Query("select count(*) from transports where status = 'FREE' and type = :type")
    Long countAvailableByType(@Param("type") TransportType type);
}
