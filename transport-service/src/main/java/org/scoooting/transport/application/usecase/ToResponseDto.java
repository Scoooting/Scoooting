package org.scoooting.transport.application.usecase;

import lombok.RequiredArgsConstructor;
import org.scoooting.transport.application.ports.UserClient;
import org.scoooting.transport.domain.mappers.TransportMapper;
import org.scoooting.transport.domain.model.Transport;
import org.scoooting.transport.domain.model.TransportStatus;
import org.scoooting.transport.domain.repositories.TransportStatusRepository;
import org.scoooting.transport.adapters.interfaces.dto.TransportResponseDTO;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ToResponseDto {

    private final UserClient userClient;
    private final TransportStatusRepository statusRepository;
    private final TransportMapper transportMapper;

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
    public Mono<TransportResponseDTO> execute(Transport transport) {
        return statusRepository.findById(transport.getStatusId())
                .map(TransportStatus::getName)
                .defaultIfEmpty("UNKNOWN")
                .flatMap(statusName ->
                        userClient.getCityName(transport.getCityId())
                                .map(cityName -> transportMapper.toResponseDTO(transport, statusName, cityName))
                );
    }
}
