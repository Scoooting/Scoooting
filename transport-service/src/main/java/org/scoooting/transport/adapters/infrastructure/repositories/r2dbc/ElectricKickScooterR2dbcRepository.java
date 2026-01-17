package org.scoooting.transport.adapters.infrastructure.repositories.r2dbc;

import org.scoooting.transport.adapters.infrastructure.entities.ElectricKickScooterEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectricKickScooterR2dbcRepository extends ReactiveCrudRepository<ElectricKickScooterEntity, Long> {
}
