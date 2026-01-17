package org.scoooting.rental.application.usecase.rentals;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.application.mappers.RentalMapper;
import org.scoooting.rental.domain.model.Rental;
import org.scoooting.rental.domain.repositories.RentalRepository;
import org.scoooting.rental.application.dto.PageResponseDTO;
import org.scoooting.rental.application.dto.RentalResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RequiredArgsConstructor
public class RentalHistoryUseCase {

    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;

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
}
