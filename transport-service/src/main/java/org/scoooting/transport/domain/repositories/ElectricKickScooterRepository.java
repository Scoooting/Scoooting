package org.scoooting.transport.domain.repositories;

import org.scoooting.transport.domain.model.ElectricKickScooter;
import reactor.core.publisher.Mono;

public interface ElectricKickScooterRepository {
    Mono<ElectricKickScooter> findById(Long id);
    Mono<ElectricKickScooter> save(ElectricKickScooter electricKickScooter);
}
