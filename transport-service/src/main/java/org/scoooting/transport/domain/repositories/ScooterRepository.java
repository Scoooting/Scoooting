package org.scoooting.transport.domain.repositories;

import org.scoooting.transport.domain.model.ElectricScooter;
import reactor.core.publisher.Mono;

public interface ScooterRepository {
    Mono<ElectricScooter> findById(Long id);
    Mono<ElectricScooter> save(ElectricScooter electricScooter);
}
