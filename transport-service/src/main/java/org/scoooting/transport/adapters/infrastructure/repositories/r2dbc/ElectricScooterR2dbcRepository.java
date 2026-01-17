package org.scoooting.transport.adapters.infrastructure.repositories.r2dbc;

import org.scoooting.transport.adapters.infrastructure.entities.ElectricScooterEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectricScooterR2dbcRepository extends ReactiveCrudRepository<ElectricScooterEntity, Long> {
}
