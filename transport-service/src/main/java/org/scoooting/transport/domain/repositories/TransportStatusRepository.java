package org.scoooting.transport.domain.repositories;

import org.scoooting.transport.domain.model.TransportStatus;
import reactor.core.publisher.Mono;

public interface TransportStatusRepository {
    Mono<TransportStatus> findById(Long id);
    Mono<TransportStatus> findByName(String name);
    Mono<TransportStatus> save(TransportStatus transportStatus);
}
