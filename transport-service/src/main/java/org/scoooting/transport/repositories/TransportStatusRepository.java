package org.scoooting.transport.repositories;

import org.scoooting.transport.entities.TransportStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface TransportStatusRepository extends ReactiveCrudRepository<TransportStatus, Long> {
    Mono<TransportStatus> findByName(String name);
}