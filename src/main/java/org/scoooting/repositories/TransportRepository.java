package org.scoooting.repositories;

import org.scoooting.entities.Transport;
import org.scoooting.entities.enums.TransportType;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportRepository extends CrudRepository<Transport, Long> {

    @Query("""
        SELECT * FROM transports 
        WHERE status_id = (SELECT id FROM transport_statuses WHERE name = 'AVAILABLE')
        AND latitude BETWEEN :latMin AND :latMax
        AND longitude BETWEEN :lngMin AND :lngMax
        """)
    List<Transport> findAvailableInArea(Double latMin, Double latMax, Double lngMin, Double lngMax);

    @Query("""
        SELECT * FROM transports 
        WHERE transport_type = CAST(:type AS VARCHAR)
        AND status_id = (SELECT id FROM transport_statuses WHERE name = 'AVAILABLE')
        AND latitude BETWEEN :latMin AND :latMax
        AND longitude BETWEEN :lngMin AND :lngMax
        """)
    List<Transport> findAvailableByTypeInArea(
            TransportType type, Double latMin, Double latMax, Double lngMin, Double lngMax
    );

    @Query("""
        SELECT * FROM transports 
        WHERE transport_type = CAST(:type AS VARCHAR)
        AND status_id = (SELECT id FROM transport_statuses WHERE name = 'AVAILABLE')
        """)
    List<Transport> findAvailableByType(TransportType type);

    @Query("""
        SELECT COUNT(*) FROM transports 
        WHERE transport_type = CAST(:type AS VARCHAR)
        AND status_id = (SELECT id FROM transport_statuses WHERE name = 'AVAILABLE')
        """)
    long countAvailableByType(TransportType type);

    @Query("SELECT * FROM transports ORDER BY id LIMIT :limit OFFSET :offset")
    List<Transport> findAllWithPagination(int offset, int limit);
}