package org.scoooting.transport.domain.repositories;

import org.scoooting.transport.domain.model.Transport;
import org.scoooting.transport.domain.model.enums.TransportType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransportRepository {

    Mono<Transport> findById(Long id);

    Mono<Transport> save(Transport transport);

    Flux<Transport> findAvailableInArea(Double latMin, Double latMax, Double lngMin, Double lngMax);

    Flux<Transport> findAvailableByTypeInArea(
            TransportType type, Double latMin, Double latMax, Double lngMin, Double lngMax
    );

    Flux<Transport> findAvailableByType(TransportType type);

    Mono<Long> countAvailableByType(TransportType type);
}
