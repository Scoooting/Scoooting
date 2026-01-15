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
import org.scoooting.rental.application.dto.TransportResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Instant;

@RequiredArgsConstructor
public class CancelRentalUseCase {

    private final RentalRepository rentalRepository;
    private final RentalStatusRepository rentalStatusRepository;
    private final TransportClient transportClient;
    private final RentalMapper rentalMapper;

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
        TransportResponseDTO transport = transportClient.getTransport(rental.getTransportId());

        transportClient.updateTransportStatus(transport.id(), "AVAILABLE");

        RentalResponseDTO rentalResponseDTO = rentalMapper.toResponseDTO(rental);
        rentalResponseDTO.setTransportType(transport.type());
        rentalResponseDTO.setStatus("Отменена");

        return rentalResponseDTO;
    }
}
