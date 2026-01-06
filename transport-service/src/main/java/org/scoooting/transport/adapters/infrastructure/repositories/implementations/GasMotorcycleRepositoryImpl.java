package org.scoooting.transport.adapters.infrastructure.repositories.implementations;

import lombok.RequiredArgsConstructor;
import org.scoooting.transport.adapters.infrastructure.repositories.mappers.GasMotorcycleMapper;
import org.scoooting.transport.adapters.infrastructure.repositories.r2dbc.GasMotorcycleR2dbcRepository;
import org.scoooting.transport.domain.model.ElectricScooter;
import org.scoooting.transport.domain.model.GasMotorcycle;
import org.scoooting.transport.domain.repositories.GasMotorcycleRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class GasMotorcycleRepositoryImpl implements GasMotorcycleRepository {

    private final GasMotorcycleR2dbcRepository repository;
    private final GasMotorcycleMapper mapper;

    @Override
    public Mono<GasMotorcycle> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<GasMotorcycle> save(GasMotorcycle gasMotorcycle) {
        return repository.save(mapper.toEntity(gasMotorcycle)).map(mapper::toDomain);
    }

}
