package org.scoooting.rental.application.usecase.rentals;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.application.mappers.RentalMapper;
import org.scoooting.rental.domain.exceptions.DataNotFoundException;
import org.scoooting.rental.domain.model.Rental;
import org.scoooting.rental.domain.repositories.RentalRepository;
import org.scoooting.rental.application.dto.PageResponseDTO;
import org.scoooting.rental.application.dto.RentalResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RequiredArgsConstructor
public class GetActiveRentalUseCase {

    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;

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
}
