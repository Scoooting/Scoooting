package org.scoooting.rental.application.usecase.rentals;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.application.mappers.RentalMapper;
import org.scoooting.rental.application.ports.TransportClient;
import org.scoooting.rental.domain.exceptions.DataNotFoundException;
import org.scoooting.rental.domain.model.Rental;
import org.scoooting.rental.domain.model.RentalStatus;
import org.scoooting.rental.domain.repositories.RentalRepository;
import org.scoooting.rental.domain.repositories.RentalStatusRepository;
import org.scoooting.rental.application.dto.RentalResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;

@RequiredArgsConstructor
public class StartRentalUseCase {

    private final RentalRepository rentalRepository;
    private final RentalStatusRepository rentalStatusRepository;
    private final TransportClient transportClient;
    private final RentalMapper rentalMapper;

    /**
     * Start new rental (reactive wrapper).
     *
     * Wraps blocking JDBC code in Mono.fromCallable() with boundedElastic scheduler:
     * - boundedElastic scheduler provides separate thread pool for blocking operations
     * - Prevents blocking reactive event loop threads
     * - Actual transaction logic is in startRentalBlocking()
     */
    public Mono<RentalResponseDTO> startRental(Long userId, Long transportId, Double startLat, Double startLng) {
        return Mono.fromCallable(() -> startRentalBlocking(userId, transportId, startLat, startLng))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Start new rental - blocking implementation.
     *
     * TRANSACTION IS CRITICAL:
     * - Multiple queries across multiple operations:
     *   1. SELECT check if user has active rental
     *   2. HTTP GET validate user exists
     *   3. HTTP GET validate transport exists
     *   4. SELECT rental status by name
     *   5. INSERT new rental
     *   6. HTTP PUT update transport status to IN_USE
     *
     * WHY transaction is critical:
     * 1. ATOMICITY: All operations must succeed or all rollback
     *    - If transport update fails, rental should NOT be created
     *    - If user validation fails, rental should NOT be created
     *    - Database remains consistent even if external HTTP calls fail
     *
     * 2. CONSISTENCY: Prevents double-booking race condition
     *    Without transaction:
     *    Thread 1 (User A): SELECT active rental for transport 5 → none found
     *    Thread 2 (User B): SELECT active rental for transport 5 → none found
     *    Thread 1: INSERT rental for transport 5
     *    Thread 2: INSERT rental for transport 5 ← CONFLICT! Same transport rented twice!
     *
     *    With transaction (READ_COMMITTED isolation):
     *    Thread 1: BEGIN, SELECT active rental (no results)
     *    Thread 2: BEGIN, SELECT active rental (no results) ← doesn't see uncommitted Thread 1 INSERT
     *    Thread 1: INSERT rental, update transport, COMMIT
     *    Thread 2: INSERT rental ← Should add check: verify transport available before INSERT
     *    Better: Add unique constraint on (transport_id, user_id, status='ACTIVE')
     *
     * 3. ISOLATION: Other transactions don't see partial state
     *    - During rental creation, nobody can see half-created rental
     *    - Transport stays AVAILABLE until entire rental creation commits
     *
     * 4. ROLLBACK on failure:
     *    - If HTTP call to transport-service fails after INSERT
     *    - Transaction rollback removes the inserted rental
     *    - No orphaned records
     *
     * NOTE: HTTP calls inside transaction are DANGEROUS:
     * - External HTTP calls can take seconds → transaction held open too long
     * - If transport-service is slow/down, database connection blocked
     * - Can exhaust connection pool under load
     * - BETTER PATTERN: Use saga pattern or event-driven architecture
     *   1. Create rental with PENDING status
     *   2. Commit transaction
     *   3. Make HTTP calls asynchronously
     *   4. Update rental to ACTIVE or FAILED based on results
     *
     * @throws IllegalStateException if user already has active rental
     */
    @Transactional
    protected RentalResponseDTO startRentalBlocking(Long userId, Long transportId, Double startLat, Double startLng) {
        // Check if we have active rental
        if (rentalRepository.findActiveRentalByUserId(userId).isPresent()) {
            throw new IllegalStateException("User already has an active rental");
        }

        RentalStatus rentalStatus = rentalStatusRepository.findByName("ACTIVE")
                .orElseThrow(() -> new DataNotFoundException("ACTIVE status not found"));

        // Create rental
        Rental rental = Rental.builder()
                .userId(userId)
                .transportId(transportId)
                .statusId(rentalStatus.getId())
                .startTime(Instant.now())
                .startLatitude(startLat)
                .startLongitude(startLng)
                .build();
        rental = rentalRepository.save(rental);

        transportClient.updateTransportStatus(transportId, "IN_USE");

        return rentalMapper.toResponseDTO(rental);
    }
}
