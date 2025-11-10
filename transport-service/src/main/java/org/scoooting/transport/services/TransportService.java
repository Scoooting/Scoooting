package org.scoooting.transport.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.transport.clients.resilient.ResilientUserClient;
import org.scoooting.transport.dto.request.UpdateCoordinatesDTO;
import org.scoooting.transport.dto.response.ScrollResponseDTO;
import org.scoooting.transport.dto.response.TransportResponseDTO;
import org.scoooting.transport.entities.Transport;
import org.scoooting.transport.entities.TransportStatus;
import org.scoooting.transport.entities.enums.TransportType;
import org.scoooting.transport.exceptions.DataNotFoundException;
import org.scoooting.transport.exceptions.TransportNotFoundException;
import org.scoooting.transport.mappers.TransportMapper;
import org.scoooting.transport.repositories.TransportRepository;
import org.scoooting.transport.repositories.TransportStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransportService {

    private final TransportRepository transportRepository;
    private final TransportStatusRepository statusRepository;
    private final TransportMapper transportMapper;
    private final ResilientUserClient resilientUserClient;
    private final TransactionalOperator transactionalOperator;

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
                .flatMap(this::toResponseDTO)
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
                .flatMap(this::toResponseDTO)
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
                .flatMap(this::toResponseDTO)
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
                .flatMap(this::toResponseDTO)
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

    /**
     * Update transport status (e.g., AVAILABLE → IN_USE).
     *
     * TRANSACTION IS CRITICAL:
     * - 3 queries: SELECT transport + SELECT status + UPDATE transport
     * - Without transaction: race condition between SELECT and UPDATE
     *   Example: Two users try to rent same transport simultaneously
     *   Thread 1: SELECT transport (status=AVAILABLE)
     *   Thread 2: SELECT transport (status=AVAILABLE) ← BOTH see AVAILABLE
     *   Thread 1: UPDATE status=IN_USE
     *   Thread 2: UPDATE status=IN_USE ← CONFLICT! Lost update!
     * - With transaction: database-level locking prevents concurrent modifications
     *
     * HOW TransactionalOperator works:
     * - Wraps entire chain in BEGIN/COMMIT
     * - Database applies row-level locks during SELECT FOR UPDATE (implicit in transaction)
     * - Other transactions WAIT until first transaction commits
     * - Ensures atomicity: all 3 queries succeed or all rollback
     *
     * @param transportId ID of transport to update
     * @param statusName new status name (AVAILABLE, IN_USE, MAINTENANCE)
     * @return updated transport DTO
     * @throws TransportNotFoundException if transport doesn't exist
     * @throws DataNotFoundException if status doesn't exist
     */
    public Mono<TransportResponseDTO> updateTransportStatus(Long transportId, String statusName) {
        return transportRepository.findById(transportId)
            .switchIfEmpty(Mono.error(new TransportNotFoundException("Transport not found")))
            .flatMap(transport -> statusRepository.findByName(statusName)
            .switchIfEmpty(Mono.error(new DataNotFoundException("Status not found")))
            .flatMap(status -> {
                transport.setStatusId(status.getId());
                return transportRepository.save(transport);
            }))
            .as(transactionalOperator::transactional)
            .flatMap(this::toResponseDTO);
    }

    /**
     * Update transport GPS coordinates.
     *
     * TRANSACTION IS CRITICAL:
     * - 2 queries: SELECT transport + UPDATE coordinates
     * - Without transaction: race condition between SELECT and UPDATE
     *   Example: GPS update conflict
     *   Thread 1: SELECT transport (coords: 50.0, 30.0)
     *   Thread 2: SELECT transport (coords: 50.0, 30.0)
     *   Thread 1: UPDATE coords to (50.1, 30.1)
     *   Thread 2: UPDATE coords to (50.2, 30.2) ← Overwrites Thread 1!
     * - With transaction: database locks the row during UPDATE
     *
     * WHY TransactionalOperator is needed:
     * - Ensures atomicity: if validation fails after SELECT, no UPDATE happens
     * - Provides isolation: other transactions see old coords until commit
     * - Prevents lost updates through database-level locking
     *
     * Returns updated DTO instead of 204 No Content for better client experience:
     * - Client can immediately see result without additional GET request
     * - Useful for debugging and testing
     * - More RESTful (resource representation in response)
     *
     * @param dto coordinates update request with validation
     * @return updated transport with new coordinates
     * @throws TransportNotFoundException if transport doesn't exist
     * @throws IllegalArgumentException if coordinates are invalid
     */
    public Mono<TransportResponseDTO> updateCoordinates(UpdateCoordinatesDTO dto) {
        if (dto.latitude() < -90 || dto.latitude() > 90) {
            return Mono.error(new IllegalArgumentException(
                    "Latitude must be between -90 and 90, got: " + dto.latitude()
            ));
        }
        if (dto.longitude() < -180 || dto.longitude() > 180) {
            return Mono.error(new IllegalArgumentException(
                    "Longitude must be between -180 and 180, got: " + dto.longitude()
            ));
        }

        return transportRepository.findById(dto.transportId())
                .switchIfEmpty(Mono.error(
                        new TransportNotFoundException("Transport with id " + dto.transportId() + " not found")
                ))
                .flatMap(transport -> {
                    transport.setLatitude(dto.latitude());
                    transport.setLongitude(dto.longitude());
                    return transportRepository.save(transport);
                })
                .as(transactionalOperator::transactional)  // ← Atomic: SELECT + UPDATE
                .flatMap(this::toResponseDTO);  // return updated obj
    }

    /**
     * Convert Transport entity to DTO with joined data.
     *
     * TRANSACTION NOT NEEDED DIRECTLY:
     * - This is a private helper method called from within other transactional methods
     * - Makes 2 queries: SELECT status + HTTP call to user-service
     * - When called from transactional method, inherits that transaction
     * - When called from non-transactional method, each query runs in separate mini-transaction
     *
     * WHY NO @Transactional here:
     * - Private methods cannot be proxied by Spring AOP
     * - Adding annotation would have no effect
     * - Relies on caller's transaction context
     *
     * N+1 PROBLEM:
     * - When called in a loop (e.g., for 20 transports), makes 20 × 2 = 40 queries
     * - This is why parent methods need TransactionalOperator
     * - Alternative: implement batch loading to reduce to 2 queries total
     *
     * @param transport entity to convert
     * @return DTO with status name and city name
     */
    public Mono<TransportResponseDTO> toResponseDTO(Transport transport) {
        return statusRepository.findById(transport.getStatusId())
                .map(TransportStatus::getName)
                .defaultIfEmpty("UNKNOWN")
                .flatMap(statusName ->
                    resilientUserClient.getCityName(transport.getCityId())
                    .map(cityName -> transportMapper.toResponseDTO(transport, statusName, cityName))
                );
    }
}