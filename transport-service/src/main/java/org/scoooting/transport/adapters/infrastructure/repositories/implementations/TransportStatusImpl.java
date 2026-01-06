package org.scoooting.transport.adapters.infrastructure.repositories.implementations;

import lombok.RequiredArgsConstructor;
import org.scoooting.transport.adapters.infrastructure.repositories.mappers.TransportStatusMapper;
import org.scoooting.transport.adapters.infrastructure.repositories.r2dbc.TransportStatusR2dbcRepository;
import org.scoooting.transport.domain.model.TransportStatus;
import org.scoooting.transport.domain.repositories.TransportStatusRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class TransportStatusImpl implements TransportStatusRepository {

    private final TransportStatusR2dbcRepository repository;
    private final TransportStatusMapper mapper;

    @Override
    public Mono<TransportStatus> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<TransportStatus> save(TransportStatus transportStatus) {
        return repository.save(mapper.toEntity(transportStatus)).map(mapper::toDomain);
    }

    @Override
    public Mono<TransportStatus> findByName(String name) {
        return repository.findByName(name).map(mapper::toDomain);
    }

}
