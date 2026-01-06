package org.scoooting.transport.adapters.infrastructure.repositories.implementations;

import lombok.RequiredArgsConstructor;
import org.scoooting.transport.adapters.infrastructure.repositories.mappers.TransportEntityMapper;
import org.scoooting.transport.adapters.infrastructure.repositories.r2dbc.TransportR2dbcRepository;
import org.scoooting.transport.domain.model.Transport;
import org.scoooting.transport.domain.model.enums.TransportType;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class TransportRepositoryImpl implements org.scoooting.transport.domain.repositories.TransportRepository {

    private final TransportR2dbcRepository repository;
    private final TransportEntityMapper mapper;

    @Override
    public Mono<Transport> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<Transport> save(Transport transport) {
        return repository.save(mapper.toEntity(transport)).map(mapper::toDomain);
    }

    @Override
    public Flux<Transport> findAvailableInArea(Double latMin, Double latMax, Double lngMin, Double lngMax) {
        return repository.findAvailableInArea(latMin, latMax, lngMin, lngMax)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Transport> findAvailableByTypeInArea(TransportType type, Double latMin, Double latMax, Double lngMin, Double lngMax) {
        return repository.findAvailableByTypeInArea(type, latMin, latMax, lngMin, lngMax)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Transport> findAvailableByType(TransportType type) {
        return repository.findAvailableByType(type).map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countAvailableByType(TransportType type) {
        return repository.countAvailableByType(type);
    }
}
