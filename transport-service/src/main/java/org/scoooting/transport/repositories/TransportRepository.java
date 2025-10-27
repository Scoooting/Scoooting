package org.scoooting.transport.repositories;

import org.scoooting.transport.entities.Transport;
import org.scoooting.transport.entities.enums.TransportType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TransportRepository extends ReactiveCrudRepository<Transport, Long> {

    @Query("""
        SELECT * FROM transports 
        WHERE status_id = (SELECT id FROM transport_statuses WHERE name = 'AVAILABLE')
        AND latitude BETWEEN :latMin AND :latMax
        AND longitude BETWEEN :lngMin AND :lngMax
        """)
    Flux<Transport> findAvailableInArea(Double latMin, Double latMax, Double lngMin, Double lngMax);

    @Query("""
        SELECT * FROM transports 
        WHERE transport_type = CAST(:type AS VARCHAR)
        AND status_id = (SELECT id FROM transport_statuses WHERE name = 'AVAILABLE')
        AND latitude BETWEEN :latMin AND :latMax
        AND longitude BETWEEN :lngMin AND :lngMax
        """)
    Flux<Transport> findAvailableByTypeInArea(
            TransportType type, Double latMin, Double latMax, Double lngMin, Double lngMax
    );

    @Query("""
        SELECT * FROM transports 
        WHERE transport_type = CAST(:type AS VARCHAR)
        AND status_id = (SELECT id FROM transport_statuses WHERE name = 'AVAILABLE')
        """)
    Flux<Transport> findAvailableByType(TransportType type);

    @Query("""
        SELECT COUNT(*) FROM transports 
        WHERE transport_type = CAST(:type AS VARCHAR)
        AND status_id = (SELECT id FROM transport_statuses WHERE name = 'AVAILABLE')
        """)
    Mono<Long> countAvailableByType(TransportType type);
}