package org.scoooting.transport.adapters.infrastructure.repositories.r2dbc;

import org.scoooting.transport.adapters.infrastructure.entities.TransportStatusEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface TransportStatusR2dbcRepository extends ReactiveCrudRepository<TransportStatusEntity, Long> {
    Mono<TransportStatusEntity> findByName(String name);
}