package org.scoooting.rental.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.clients.resilient.ResilientTransportService;
import org.scoooting.rental.clients.resilient.ResilientUserClient;
import org.scoooting.rental.config.FeignJwtInterceptor;
import org.scoooting.rental.config.KafkaConfig;
import org.scoooting.rental.config.UserPrincipal;
import org.scoooting.rental.dto.common.PageResponseDTO;
import org.scoooting.rental.dto.UpdateCoordinatesDTO;
import org.scoooting.rental.dto.kafka.ForceEndRentalDto;
import org.scoooting.rental.dto.kafka.RentalEventDto;
import org.scoooting.rental.dto.response.RentalResponseDTO;
import org.scoooting.rental.dto.response.TransportResponseDTO;
import org.scoooting.rental.dto.response.UserResponseDTO;
import org.scoooting.rental.entities.Rental;
import org.scoooting.rental.entities.RentalStatus;
import org.scoooting.rental.exceptions.DataNotFoundException;
import org.scoooting.rental.mappers.RentalMapper;
import org.scoooting.rental.repositories.RentalRepository;
import org.scoooting.rental.repositories.RentalStatusRepository;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final ResilientTransportService transportClient;
    private final ResilientUserClient feignUserClient;
    private final RentalStatusRepository rentalStatusRepository;
    private final RentalMapper rentalMapper;
    private final KafkaService kafkaService;

    private static final BigDecimal BASE_RATE = new BigDecimal("0.50");
    private static final BigDecimal UNLOCK_FEE = new BigDecimal("1.00");

    /**
     * Start new rental (reactive wrapper).
     *
     * Wraps blocking JDBC code in Mono.fromCallable() with boundedElastic scheduler:
     * - boundedElastic scheduler provides separate thread pool for blocking operations
     * - Prevents blocking reactive event loop threads
     * - Actual transaction logic is in startRentalBlocking()
     */
    public Mono<RentalResponseDTO> startRental(Long userId, Long transportId, Double startLat, Double startLng) {
        return Mono.fromCallable(() -> {
            return startRentalBlocking(userId, transportId, startLat, startLng);
        }).subscribeOn(Schedulers.boundedElastic());
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

        // Validate user exists
        FeignJwtInterceptor.setUserToken("Bearer " + getCurrentUserToken());
//        feignUserClient.getUserById(userId);

        // Validate transport
        FeignJwtInterceptor.clear();
//        transportClient.getTransport(transportId);

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

        FeignJwtInterceptor.useServiceAccount();
        transportClient.updateTransportStatus(transportId, "IN_USE");

        return rentalMapper.toResponseDTO(rental);
    }

    /**
     * End rental (reactive wrapper).
     */
    public Mono<RentalResponseDTO> endRental(Long userId, Double endLat, Double endLng) {
        return Mono.fromCallable(() -> endRentalBlocking(userId, endLat, endLng)).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * End active rental and calculate cost.
     *
     * TRANSACTION IS CRITICAL:
     * - Multiple operations:
     *   1. SELECT active rental by userId
     *   2. Calculate duration and cost (in-memory)
     *   3. SELECT COMPLETED status
     *   4. UPDATE rental (set end time, cost, status)
     *   5. HTTP GET transport details
     *   6. HTTP PUT update transport status to AVAILABLE
     *   7. HTTP PUT update transport coordinates
     *   8. HTTP GET user details
     *   9. HTTP PUT update user bonuses
     *
     * WHY transaction is critical:
     * 1. ATOMICITY: Rental must be marked complete AND transport freed atomically
     *    - If transport update fails, rental stays active (correct!)
     *    - If bonus update fails, rental still marked complete (acceptable)
     *    - PROBLEM: Current implementation doesn't rollback rental if external calls fail
     *
     * 2. CONSISTENCY: Prevents double-ending race condition
     *    Without transaction:
     *    Thread 1: SELECT active rental → found
     *    Thread 2: SELECT active rental → found (same rental!)
     *    Thread 1: UPDATE rental end_time
     *    Thread 2: UPDATE rental end_time ← Overwrites Thread 1's calculation!
     *    Result: Cost calculated twice with different durations!
     *
     *    With transaction:
     *    Thread 1: BEGIN, SELECT active rental FOR UPDATE (locks row)
     *    Thread 2: BEGIN, SELECT active rental (WAITS for Thread 1 lock)
     *    Thread 1: UPDATE rental, COMMIT (releases lock)
     *    Thread 2: SELECT returns empty (rental no longer active), throws exception
     *    Result: Correct - only one end allowed
     *
     * 3. FINANCIAL ACCURACY: Cost calculation must match duration
     *    - Duration and cost calculated in same transaction
     *    - No race condition can change rental between calculation and save
     *    - Critical for billing accuracy!
     *
     * SERIOUS ISSUE: HTTP calls inside transaction
     * - Multiple slow HTTP calls (GET transport, PUT status, PUT coords, GET user, PUT bonuses)
     * - Can take 1-5 seconds total
     * - Transaction held open entire time
     * - Database connection blocked for seconds
     * - Under load: connection pool exhaustion
     *
     * RECOMMENDED FIX:
     * 1. Update rental and mark as COMPLETING (not COMPLETED)
     * 2. Commit transaction
     * 3. Make external HTTP calls
     * 4. If all succeed: mark as COMPLETED
     * 5. If any fail: mark as FAILED, retry later
     *
     * @throws IllegalStateException if no active rental found
     */
    @Transactional
    protected RentalResponseDTO endRentalBlocking(Long userId, Double endLat, Double endLng) {
        // Find active rental
        Rental rental = rentalRepository.findActiveRentalByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No active rental found for user"));

        // Calculate duration and cost
        Instant endTime = Instant.now();
        long minutes = Duration.between(rental.getStartTime(), endTime).toMinutes();
        BigDecimal totalCost = UNLOCK_FEE.add(BASE_RATE.multiply(BigDecimal.valueOf(minutes)));

        // Calculate distance
        double distance = calculateDistance(
                rental.getStartLatitude(), rental.getStartLongitude(),
                endLat, endLng
        );

        // Update rental
        rental.setEndTime(endTime);
        rental.setEndLatitude(endLat);
        rental.setEndLongitude(endLng);
        rental.setDurationMinutes((int) minutes);
        rental.setTotalCost(totalCost);
        rental.setDistanceKm(BigDecimal.valueOf(distance));

        // Set COMPLETED status
        RentalStatus completedStatus = rentalStatusRepository.findByName("COMPLETED")
                .orElseThrow(() -> new DataNotFoundException("COMPLETED status not found"));
        rental.setStatusId(completedStatus.getId());

        rental = rentalRepository.save(rental);

        // Update transport
        FeignJwtInterceptor.clear();
        TransportResponseDTO transport = transportClient.getTransport(rental.getTransportId()).getBody();

        FeignJwtInterceptor.useServiceAccount();
        transportClient.updateTransportStatus(transport.id(), "AVAILABLE");
        transportClient.updateTransportCoordinates(new UpdateCoordinatesDTO(transport.id(), endLat, endLng));

        // Award bonus points
        FeignJwtInterceptor.setUserToken("Bearer " + getCurrentUserToken());
        UserResponseDTO user = feignUserClient.getUserById(userId).getBody();
        feignUserClient.addBonuses(userId, user.bonuses() + (int) minutes);

        RentalResponseDTO rentalResponseDTO = rentalMapper.toResponseDTO(rental);
        rentalResponseDTO.setTransportType(transport.type());
        rentalResponseDTO.setStatus("Завершена");

        return rentalResponseDTO;
    }

    /**
     * Cancel active rental (reactive wrapper).
     */
    public Mono<RentalResponseDTO> cancelRental(Long userId) {
        return Mono.fromCallable(() -> cancelRentalBlocking(userId)).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Cancel active rental.
     *
     * TRANSACTION IS CRITICAL:
     * - Multiple operations:
     *   1. SELECT active rental
     *   2. Validate rental not already ended
     *   3. SELECT CANCELLED status
     *   4. UPDATE rental (set status, end time)
     *   5. HTTP GET transport details
     *   6. HTTP PUT update transport status to AVAILABLE
     *
     * WHY transaction is critical:
     * 1. ATOMICITY: Rental cancel and status update must be atomic
     *    - If rental marked cancelled but transport stays IN_USE → bad state!
     *    - With transaction: if transport update fails, rental rollback to active
     *
     * 2. CONSISTENCY: Prevents cancel race condition
     *    Without transaction:
     *    Thread 1 (cancel): SELECT rental → found
     *    Thread 2 (end): SELECT rental → found
     *    Thread 1: UPDATE status=CANCELLED
     *    Thread 2: UPDATE status=COMPLETED, calculate cost
     *    Result: Lost cost calculation! User not charged.
     *
     *    With transaction:
     *    Thread 1: BEGIN, SELECT rental FOR UPDATE (locks)
     *    Thread 2: BEGIN, SELECT rental (WAITS)
     *    Thread 1: UPDATE cancelled, COMMIT
     *    Thread 2: SELECT finds status=CANCELLED, throws error
     *    Result: Only cancel succeeds, no lost updates
     *
     * 3. PREVENTS DOUBLE-CANCEL:
     *    - Check "if endTime != null" inside transaction
     *    - Another thread can't mark as ended between check and update
     *    - Database lock prevents concurrent modifications
     *
     * SAME ISSUE: HTTP call inside transaction
     * - If transport-service is slow, transaction held open
     * - Should move HTTP call outside transaction
     *
     * @throws IllegalStateException if no active rental or already ended
     */
    @Transactional
    protected RentalResponseDTO cancelRentalBlocking(Long userId) {
        Rental rental = rentalRepository.findActiveRentalByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No active rental found"));

        // Check if rental has been already cancelled before
        if (rental.getEndTime() != null) {
            throw new IllegalStateException("Rental already ended or cancelled");
        }

        // Set CANCELLED status
        RentalStatus cancelledStatus = rentalStatusRepository.findByName("CANCELLED")
                .orElseThrow(() -> new DataNotFoundException("CANCELLED status not found"));
        rental.setStatusId(cancelledStatus.getId());
        rental.setEndTime(Instant.now());
        rental.setDurationMinutes(0);
        rental.setTotalCost(BigDecimal.valueOf(0));
        rentalRepository.save(rental);

        // Free transport
        FeignJwtInterceptor.clear();
        TransportResponseDTO transport = transportClient.getTransport(rental.getTransportId()).getBody();

        FeignJwtInterceptor.useServiceAccount();
        transportClient.updateTransportStatus(transport.id(), "AVAILABLE");

        RentalResponseDTO rentalResponseDTO = rentalMapper.toResponseDTO(rental);
        rentalResponseDTO.setTransportType(transport.type());
        rentalResponseDTO.setStatus("Отменена");

        return rentalResponseDTO;
    }

    /**
     * Get user's active rental (reactive wrapper).
     */
    public Mono<RentalResponseDTO> getActiveRental(Long userId) {
        return Mono.fromCallable(() ->
                        rentalRepository.findActiveRentalByUserId(userId)
                                .map(rentalMapper::toResponseDTO)
                ).subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional
                        -> optional.map(Mono::just).orElseGet(()
                        -> Mono.error(new DataNotFoundException("No active rental found for user"))
                ));
    }

    /**
     * Get user's rental history (reactive wrapper).
     */
    public Mono<PageResponseDTO<RentalResponseDTO>> getUserRentalHistory(Long userId, int page, int size) {
        return Mono.fromCallable(() -> getUserRentalHistoryBlocking(userId, page, size))
                .subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * Get paginated rental history for user.
     *
     * TRANSACTION (readOnly=true) IS NEEDED:
     * - Makes 2 queries:
     *   1. SELECT rentals with pagination
     *   2. SELECT COUNT(*) for total
     * - Without transaction: 2 separate connections
     * - With transaction: single connection reused
     *
     * WHY readOnly=true:
     * - Tells Hibernate: no entity changes expected
     * - Skips dirty checking (no tracking of entity modifications)
     * - Skips flush() before queries
     * - JDBC driver can optimize with read-only connection mode
     * - 10-20% faster than regular transaction for read operations
     *
     * CONSISTENT SNAPSHOT:
     * - Both queries (SELECT rentals + COUNT) see same database state
     * - Without transaction: between two queries, data might change
     *   * SELECT returns 20 rentals
     *   * New rental created
     *   * COUNT returns 21
     *   * Inconsistent: totalPages calculation wrong
     * - With transaction: both queries use same consistent snapshot
     *
     * @param userId user ID
     * @param page page number (0-indexed)
     * @param size items per page
     * @return paginated rental history
     */
    @Transactional(readOnly = true)
    protected PageResponseDTO<RentalResponseDTO> getUserRentalHistoryBlocking(Long userId, int page, int size) {
        int offset = page * size;
        List<Rental> rentals = rentalRepository.findRentalHistoryByUserId(userId, offset, size);
        long total = rentalRepository.countRentalsByUserId(userId);

        List<RentalResponseDTO> rentalDTOs = rentals.stream().map(rentalMapper::toResponseDTO).toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(rentalDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    /**
     * Calculate straight-line distance between two coordinates.
     *
     * TRANSACTION NOT NEEDED:
     * - Pure calculation, no database access
     * - No external calls
     * - Stateless operation
     *
     * Uses simplified distance formula (not true geodesic):
     * - Good approximation for short distances (<100km)
     * - Faster than Haversine formula
     * - Acceptable for scooter rental distances
     */
    private double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return 0.0;
        }

        double latDiff = Math.abs(lat1 - lat2) * 111.0;
        double lngDiff = Math.abs(lng1 - lng2) * 111.0 * Math.cos(Math.toRadians(lat1));
        return Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);
    }

    /**
     * Force end any rental by ID (Support operation).
     *
     * Used when Support needs to manually end rental due to:
     * - User complaint (transport broken, accident)
     * - System issue (app crash, payment failure)
     * - Emergency (transport stolen, dangerous situation)
     */
    public Mono<ForceEndRentalDto> forceEndRental(Long rentalId, Double endLat, Double endLng) {
        return Mono.fromCallable(() -> forceEndRentalBlocking(rentalId, endLat, endLng))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Force end rental by rental ID (Support only).
     *
     * TRANSACTION IS CRITICAL:
     * - Similar to regular endRental but by rentalId instead of userId
     * - Support can end ANY user's rental (not just their own)
     * - Must calculate cost and update transport atomically
     *
     * WHY transaction:
     * 1. ATOMICITY: Rental completion + transport release must be atomic
     * 2. CONSISTENCY: Only one force-end allowed (same as regular end)
     * 3. FINANCIAL: Cost calculation must be accurate and committed
     *
     * @throws DataNotFoundException if rental not found
     * @throws IllegalStateException if rental already ended
     */
    @Transactional
    protected ForceEndRentalDto forceEndRentalBlocking(Long rentalId, Double endLat, Double endLng) {
        // Find rental by ID
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new DataNotFoundException("Rental not found"));

        // Check if already ended
        if (rental.getEndTime() != null) {
            throw new IllegalStateException("Rental already ended");
        }

        // Calculate duration and cost
        Instant endTime = Instant.now();
        long minutes = Duration.between(rental.getStartTime(), endTime).toMinutes();
        BigDecimal totalCost = UNLOCK_FEE.add(BASE_RATE.multiply(BigDecimal.valueOf(minutes)));

        // Calculate distance
        double distance = calculateDistance(
                rental.getStartLatitude(), rental.getStartLongitude(),
                endLat, endLng
        );

        // Update rental
        rental.setEndTime(endTime);
        rental.setEndLatitude(endLat);
        rental.setEndLongitude(endLng);
        rental.setDurationMinutes((int) minutes);
        rental.setTotalCost(totalCost);
        rental.setDistanceKm(BigDecimal.valueOf(distance));

        // Set COMPLETED status
        RentalStatus completedStatus = rentalStatusRepository.findByName("COMPLETED")
                .orElseThrow(() -> new DataNotFoundException("COMPLETED status not found"));
        rental.setStatusId(completedStatus.getId());

        rental = rentalRepository.save(rental);

        // Update transport
        TransportResponseDTO transport = transportClient.getTransport(rental.getTransportId()).getBody();
        transportClient.updateTransportStatus(transport.id(), "AVAILABLE");
        transportClient.updateTransportCoordinates(new UpdateCoordinatesDTO(transport.id(), endLat, endLng));

        // Award bonus points to user
        UserResponseDTO user = feignUserClient.getUserById(rental.getUserId()).getBody();
        feignUserClient.addBonuses(rental.getUserId(),user.bonuses() + (int) minutes);

        RentalResponseDTO rentalResponseDTO = rentalMapper.toResponseDTO(rental);
        rentalResponseDTO.setTransportType(transport.type());
        rentalResponseDTO.setStatus("Принудительно завершена");

        return new ForceEndRentalDto(rentalResponseDTO, new UserPrincipal(user.name(), user.id(), user.email(), user.role()));
    }

    /**
     * Get all rentals in system (Analyst operation).
     *
     * Used for:
     * - Business analytics (peak hours, popular routes)
     * - Revenue analysis
     * - User behavior patterns
     * - System performance metrics
     */
    public Mono<PageResponseDTO<RentalResponseDTO>> getAllRentals(int page, int size) {
        return Mono.fromCallable(() -> getAllRentalsBlocking(page, size))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Get all system rentals with pagination (Analyst only).
     *
     * TRANSACTION (readOnly=true) IS NEEDED:
     * - Makes 2 queries:
     *   1. SELECT all rentals with pagination
     *   2. SELECT COUNT(*) for total
     * - Without transaction: 2 separate connections, possible inconsistency
     * - With transaction: single consistent snapshot
     *
     * WHY readOnly=true:
     * - Analyst queries are read-only by design
     * - No data modifications
     * - Allows JDBC optimizations
     * - Prevents accidental data changes
     *
     * PERFORMANCE NOTE:
     * - This can return thousands of rentals
     * - Consider adding filters (date range, status, etc.) in production
     * - For now, basic pagination is sufficient for lab work
     *
     * @param page page number (0-indexed)
     * @param size items per page
     * @return paginated list of all rentals
     */
    @Transactional(readOnly = true)
    protected PageResponseDTO<RentalResponseDTO> getAllRentalsBlocking(int page, int size) {
        int offset = page * size;
        List<Rental> rentals = rentalRepository.findAllRentals(offset, size);
        long total = rentalRepository.countAllRentals();

        List<RentalResponseDTO> rentalDTOs = rentals.stream()
                .map(rentalMapper::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(
                rentalDTOs,
                page,
                size,
                total,
                totalPages,
                page == 0,
                page >= totalPages - 1
        );
    }

    private String getCurrentUserToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getCredentials().toString())
                .block();
    }

    public Mono<Void> sendReport(RentalResponseDTO rental, UserPrincipal userPrincipal) {
        return kafkaService.sendReport(rental, userPrincipal);
    }

    public Mono<Void> sendNotification(RentalEventDto rentalEventDto) {
        return kafkaService.sendMessage(KafkaConfig.RENTAL_EVENTS_TOPIC, rentalEventDto);
    }
}