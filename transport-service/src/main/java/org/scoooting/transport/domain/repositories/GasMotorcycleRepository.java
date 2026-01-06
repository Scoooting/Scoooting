package org.scoooting.transport.domain.repositories;

import org.scoooting.transport.domain.model.ElectricScooter;
import org.scoooting.transport.domain.model.GasMotorcycle;
import reactor.core.publisher.Mono;

public interface GasMotorcycleRepository {
    Mono<GasMotorcycle> findById(Long id);
    Mono<GasMotorcycle> save(GasMotorcycle gasMotorcycle);
}
