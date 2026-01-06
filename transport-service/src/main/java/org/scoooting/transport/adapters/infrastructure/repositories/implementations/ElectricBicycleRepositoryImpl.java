package org.scoooting.transport.adapters.infrastructure.repositories.implementations;

import lombok.RequiredArgsConstructor;
import org.scoooting.transport.adapters.infrastructure.repositories.mappers.ElectricBicycleMapper;
import org.scoooting.transport.adapters.infrastructure.repositories.r2dbc.ElectricBicycleR2dbcRepository;
import org.scoooting.transport.domain.model.ElectricBicycle;
import org.scoooting.transport.domain.repositories.ElectricBicycleRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class ElectricBicycleRepositoryImpl implements ElectricBicycleRepository {

    private final ElectricBicycleR2dbcRepository repository;
    private final ElectricBicycleMapper mapper;

    @Override
    public Mono<ElectricBicycle> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<ElectricBicycle> save(ElectricBicycle electricBicycle) {
        return repository.save(mapper.toEntity(electricBicycle)).map(mapper::toDomain);
    }

}
