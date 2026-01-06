package org.scoooting.transport.application.usecase;

import lombok.RequiredArgsConstructor;
import org.scoooting.transport.domain.exceptions.TransportNotFoundException;
import org.scoooting.transport.domain.model.enums.TransportType;
import org.scoooting.transport.domain.repositories.TransportRepository;
import org.scoooting.transport.dto.response.ScrollResponseDTO;
import org.scoooting.transport.dto.response.TransportResponseDTO;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class TransportUseCase {

    private final TransportRepository transportRepository;
    private final TransactionalOperator transactionalOperator;
    private final ToResponseDto toResponseDto;

    /**
     * Find nearest transports within specified radius.
     *
     * TRANSACTION IS NEEDED to solve N+1 problem:
     * - Each transport triggers toResponseDTO() which makes 2 additional queries:
     *   1. SELECT status by statusId
     *   2. HTTP call to user-service for city name
     * - For 20 transports: 1 + 20 + 20 = 41 separate queries/connections
     * - With TransactionalOperator: all 41 queries share the same DB connection
     *
     * HOW TransactionalOperator works:
     * - .as(transactionalOperator::transactional) wraps the entire Flux in a transaction
     * - Opens connection.beginTransaction() before first query
     * - Keeps connection open for all queries in the stream
     * - Commits with connection.commitTransaction() when Flux completes
     * - Rollbacks on error
     * - Uses Reactor Context (not ThreadLocal like @Transactional)
     *
     * IMPORTANT: Connection stays open during entire Flux execution.
     * For production with high load, we should consider batch loading to reduce to 3 queries.
     *
     * @param lat center latitude
     * @param lng center longitude
     * @param radiusKm search radius in kilometers
     * @return Flux of available transports in the area
     */
    public Flux<TransportResponseDTO> findNearestTransports(Double lat, Double lng, Double radiusKm) {
        // Calculate boundaries
        double latRange = radiusKm / 111.0;
        double lngRange = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        return transportRepository.findAvailableInArea(
                        lat - latRange, lat + latRange,
                        lng - lngRange, lng + lngRange
                )
                .flatMap(toResponseDto::execute)
                .as(transactionalOperator::transactional);
    }

    /**
     * Find transports by type within specified radius.
     *
     * TRANSACTION IS NEEDED for the same N+1 problem as findNearestTransports:
     * - Without transaction: 1 + N status queries + N HTTP calls = 1 + 2N connections
     * - With transaction: all queries in single connection
     *
     * TransactionalOperator keeps R2DBC connection open during entire Flux processing,
     * preventing connection pool exhaustion under concurrent load.
     *
     * @param type transport type filter
     * @param lat center latitude
     * @param lng center longitude
     * @param radiusKm search radius in kilometers
     * @return Flux of matching transports
     */
    public Flux<TransportResponseDTO> findTransportsByType(
            TransportType type, Double lat, Double lng, Double radiusKm
    ) {
        double latRange = radiusKm / 111.0;
        double lngRange = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        return transportRepository.findAvailableByTypeInArea(
                        type, lat - latRange, lat + latRange, lng - lngRange, lng + lngRange
                )
                .flatMap(toResponseDto::execute)
                .as(transactionalOperator::transactional);
    }

    /**
     * Get transport by ID with full details.
     *
     * TRANSACTION IS NEEDED:
     * - Makes 3 queries: SELECT transport + SELECT status + HTTP call for city
     * - Without transaction: 3 separate connections (open/close overhead)
     * - With transaction: single connection reused for all queries
     *
     * Although overhead is smaller than N+1 case, transaction still provides:
     * - Connection reuse (saves ~10-20ms per connection)
     * - Consistent read snapshot (isolation)
     * - Better resource utilization
     *
     * @param id transport ID
     * @return Mono with transport details
     * @throws TransportNotFoundException if transport doesn't exist
     * @throws IllegalArgumentException if ID is invalid
     */
    public Mono<TransportResponseDTO> getTransportById(Long id) {
        if (id == null || id <= 0) {
            return Mono.error(new IllegalArgumentException("Transport ID must be positive"));
        }

        return transportRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new TransportNotFoundException("Transport with id " + id + " not found")
                ))
                .flatMap(toResponseDto::execute)
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.error(new IllegalArgumentException("Invalid transport ID: " + id))
                )
                .as(transactionalOperator::transactional);
    }

    /**
     * Infinite scroll pagination for available transports by type.
     *
     * TRANSACTION IS NEEDED due to N+1 problem in toResponseDTO():
     * - For page with 20 transports: 1 + 20 status queries + 20 HTTP calls
     * - With transaction: all queries share same connection
     *
     * INFINITE SCROLL BENEFITS:
     * - No COUNT(*) query (faster by 50-90% on large tables)
     * - Takes size+1 records to determine hasMore without separate query
     * - Better UX for mobile apps (continuous scrolling)
     *
     * HOW THIS WORKS:
     * - Request: GET /transports/available/BIKE?page=0&size=20
     * - Database: SELECT ... LIMIT 21 OFFSET 0
     * - If 21 records returned: hasMore=true, return first 20
     * - If ≤20 records returned: hasMore=false, return all
     *
     * @param type transport type filter
     * @param page page number (0-indexed)
     * @param size items per page
     * @return ScrollResponseDTO with content and hasMore flag
     */
    public Mono<ScrollResponseDTO<TransportResponseDTO>> scrollAvailableTransportsByType(
            TransportType type, int page, int size
    ) {
        return transportRepository.findAvailableByType(type)
                .skip((long) page * size)  // skip prev pages
                .take(size + 1)                 // Get size+1 to prevent hasMore
                .flatMap(toResponseDto::execute)
                .collectList()
                .map(list -> {
                    boolean hasMore = list.size() > size;
                    List<TransportResponseDTO> content = hasMore
                            ? list.subList(0, size)
                            : list;
                    return new ScrollResponseDTO<>(content, page, size, hasMore);
                })
                .as(transactionalOperator::transactional);
    }
}
