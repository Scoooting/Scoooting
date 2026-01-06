package org.scoooting.transport.adapters.infrastructure.repositories.r2dbc;

import org.scoooting.transport.adapters.infrastructure.entities.ElectricBicycleEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectricBicycleR2dbcRepository extends ReactiveCrudRepository<ElectricBicycleEntity, Long> {
}