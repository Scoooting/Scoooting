package org.scoooting.transport.adapters.infrastructure.repositories.implementations;

import lombok.RequiredArgsConstructor;
import org.scoooting.transport.adapters.infrastructure.repositories.mappers.ElectricScooterMapper;
import org.scoooting.transport.adapters.infrastructure.repositories.r2dbc.ElectricScooterR2dbcRepository;
import org.scoooting.transport.domain.model.ElectricScooter;
import org.scoooting.transport.domain.repositories.ScooterRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class ElectricScooterRepositoryImpl implements ScooterRepository {

    private final ElectricScooterR2dbcRepository repository;
    private final ElectricScooterMapper mapper;

    @Override
    public Mono<ElectricScooter> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<ElectricScooter> save(ElectricScooter electricScooter) {
        return repository.save(mapper.toEntity(electricScooter)).map(mapper::toDomain);
    }

}
