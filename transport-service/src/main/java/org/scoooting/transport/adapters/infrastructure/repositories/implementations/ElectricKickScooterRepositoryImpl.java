package org.scoooting.transport.adapters.infrastructure.repositories.implementations;

import lombok.RequiredArgsConstructor;
import org.scoooting.transport.adapters.infrastructure.repositories.mappers.ElectricKickScooterMapper;
import org.scoooting.transport.adapters.infrastructure.repositories.r2dbc.ElectricKickScooterR2dbcRepository;
import org.scoooting.transport.domain.model.ElectricKickScooter;
import org.scoooting.transport.domain.model.ElectricScooter;
import org.scoooting.transport.domain.repositories.ElectricKickScooterRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class ElectricKickScooterRepositoryImpl implements ElectricKickScooterRepository {

    private final ElectricKickScooterR2dbcRepository repository;
    private final ElectricKickScooterMapper mapper;

    @Override
    public Mono<ElectricKickScooter> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<ElectricKickScooter> save(ElectricKickScooter electricKickScooter) {
        return repository.save(mapper.toEntity(electricKickScooter)).map(mapper::toDomain);
    }

}
