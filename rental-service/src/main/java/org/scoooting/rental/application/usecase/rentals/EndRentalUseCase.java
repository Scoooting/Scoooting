package org.scoooting.rental.application.usecase.rentals;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.application.mappers.RentalMapper;
import org.scoooting.rental.application.ports.TransportClient;
import org.scoooting.rental.application.ports.UserClient;
import org.scoooting.rental.application.services.Distance;
import org.scoooting.rental.domain.exceptions.DataNotFoundException;
import org.scoooting.rental.domain.model.Rental;
import org.scoooting.rental.domain.model.RentalStatus;
import org.scoooting.rental.domain.repositories.RentalRepository;
import org.scoooting.rental.domain.repositories.RentalStatusRepository;
import org.scoooting.rental.adapters.message.feign.dto.UpdateCoordinatesDTO;
import org.scoooting.rental.application.dto.RentalResponseDTO;
import org.scoooting.rental.application.dto.TransportResponseDTO;
import org.scoooting.rental.application.dto.UserResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
public class EndRentalUseCase {

    private final RentalRepository rentalRepository;
    private final RentalStatusRepository rentalStatusRepository;
    private final TransportClient transportClient;
    private final UserClient userClient;
    private final RentalMapper rentalMapper;

    private static final BigDecimal BASE_RATE = new BigDecimal("0.50");
    private static final BigDecimal UNLOCK_FEE = new BigDecimal("1.00");

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
        double distance = Distance.calculateDistance(
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
        TransportResponseDTO transport = transportClient.getTransport(rental.getTransportId());

        transportClient.updateTransportStatus(transport.id(), "AVAILABLE");
        transportClient.updateTransportCoordinates(new UpdateCoordinatesDTO(transport.id(), endLat, endLng));

        // Award bonus points
        UserResponseDTO user = userClient.getUserById(userId);
        userClient.addBonuses(userId, user.bonuses() + (int) minutes);

        RentalResponseDTO rentalResponseDTO = rentalMapper.toResponseDTO(rental);
        rentalResponseDTO.setTransportType(transport.type());
        rentalResponseDTO.setStatus("Завершена");

        return rentalResponseDTO;
    }
}
