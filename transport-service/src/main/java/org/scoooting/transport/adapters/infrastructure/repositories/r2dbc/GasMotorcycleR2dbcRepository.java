package org.scoooting.transport.adapters.infrastructure.repositories.r2dbc;

import org.scoooting.transport.adapters.infrastructure.entities.GasMotorcycleEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GasMotorcycleR2dbcRepository extends ReactiveCrudRepository<GasMotorcycleEntity, Long> {
}