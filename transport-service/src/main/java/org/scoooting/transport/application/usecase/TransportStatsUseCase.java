package org.scoooting.transport.application.usecase;

import lombok.RequiredArgsConstructor;
import org.scoooting.transport.domain.model.enums.TransportType;
import org.scoooting.transport.domain.repositories.TransportRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RequiredArgsConstructor
public class TransportStatsDto {

    private final TransportRepository transportRepository;

    /**
     * Get availability statistics for all transport types.
     *
     * TRANSACTION IS NOT NEEDED:
     * - Makes multiple independent COUNT(*) queries (one per transport type)
     * - No data modifications
     * - No relation between queries (no consistency requirement)
     * - Each COUNT is fast and atomic
     *
     * Transaction would only add overhead here without benefits.
     * Each count runs in its own mini-transaction from connection pool.
     *
     * @return Map of transport type to available count
     */
    public Mono<Map<String, Long>> getAvailabilityStats() {
        return Flux.fromArray(TransportType.values())
                .flatMap(type -> transportRepository.countAvailableByType(type)
                        .map(count -> Map.entry(type.name(), count)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

}
