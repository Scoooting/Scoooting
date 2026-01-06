package org.scoooting.transport.domain.repositories;

import org.scoooting.transport.domain.model.ElectricBicycle;
import reactor.core.publisher.Mono;

public interface ElectricBicycleRepository {
    Mono<ElectricBicycle> findById(Long id);
    Mono<ElectricBicycle> save(ElectricBicycle electricBicycle);
}