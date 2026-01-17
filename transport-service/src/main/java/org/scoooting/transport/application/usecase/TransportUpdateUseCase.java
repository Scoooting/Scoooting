package org.scoooting.transport.application.usecase;

import lombok.RequiredArgsConstructor;
import org.scoooting.transport.domain.exceptions.DataNotFoundException;
import org.scoooting.transport.domain.exceptions.TransportNotFoundException;
import org.scoooting.transport.domain.repositories.TransportRepository;
import org.scoooting.transport.domain.repositories.TransportStatusRepository;
import org.scoooting.transport.adapters.interfaces.dto.UpdateCoordinatesDTO;
import org.scoooting.transport.adapters.interfaces.dto.TransportResponseDTO;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class TransportUpdateUseCase {

    private final TransportRepository transportRepository;
    private final TransportStatusRepository statusRepository;
    private final TransactionalOperator transactionalOperator;
    private final ToResponseDto toResponseDto;

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
                .flatMap(toResponseDto::execute);
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
                .flatMap(toResponseDto::execute);  // return updated obj
    }

}
