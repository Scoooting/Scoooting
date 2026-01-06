package org.scoooting.transport.adapters.infrastructure.repositories.r2dbc;

import org.scoooting.transport.adapters.infrastructure.entities.TransportEntity;
import org.scoooting.transport.domain.model.enums.TransportType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TransportRepository extends ReactiveCrudRepository<TransportEntity, Long> {

    @Query("""
        SELECT * FROM transports 
        WHERE status_id = (SELECT id FROM transport_statuses WHERE name = 'AVAILABLE')
        AND latitude BETWEEN :latMin AND :latMax
        AND longitude BETWEEN :lngMin AND :lngMax
        """)
    Flux<TransportEntity> findAvailableInArea(Double latMin, Double latMax, Double lngMin, Double lngMax);

    @Query("""
        SELECT * FROM transports 
        WHERE transport_type = CAST(:type AS VARCHAR)
        AND status_id = (SELECT id FROM transport_statuses WHERE name = 'AVAILABLE')
        AND latitude BETWEEN :latMin AND :latMax
        AND longitude BETWEEN :lngMin AND :lngMax
        """)
    Flux<TransportEntity> findAvailableByTypeInArea(
            TransportType type, Double latMin, Double latMax, Double lngMin, Double lngMax
    );

    @Query("""
        SELECT * FROM transports 
        WHERE transport_type = CAST(:type AS VARCHAR)
        AND status_id = (SELECT id FROM transport_statuses WHERE name = 'AVAILABLE')
        """)
    Flux<TransportEntity> findAvailableByType(TransportType type);

    @Query("""
        SELECT COUNT(*) FROM transports 
        WHERE transport_type = CAST(:type AS VARCHAR)
        AND status_id = (SELECT id FROM transport_statuses WHERE name = 'AVAILABLE')
        """)
    Mono<Long> countAvailableByType(TransportType type);
}