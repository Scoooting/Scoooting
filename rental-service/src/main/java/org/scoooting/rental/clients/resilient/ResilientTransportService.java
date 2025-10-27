package org.scoooting.rental.clients.resilient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.rental.clients.feign.FeignTransportClient;
import org.scoooting.rental.dto.UpdateCoordinatesDTO;
import org.scoooting.rental.dto.response.TransportResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResilientTransportService {

    private final FeignTransportClient transportServiceApi;

    @CircuitBreaker(name = "transportService", fallbackMethod = "getTransportStatusIdFallback")
    public ResponseEntity<Long> getTransportStatusId(String name) {
        log.debug("Calling transport-service for status: {}", name);
        return transportServiceApi.getTransportStatusId(name);
    }

    public ResponseEntity<Long> getTransportStatusIdFallback(String name, Throwable t) {
        log.error("FALLBACK getTransportStatusId! status: {}, error: {}", name, t.getClass().getSimpleName());
        throw new RuntimeException("Transport service unavailable", t);
    }

    @CircuitBreaker(name = "transportService", fallbackMethod = "getTransportFallback")
    public ResponseEntity<TransportResponseDTO> getTransport(Long id) {
        log.debug("Calling transport-service for transportId: {}", id);
        return transportServiceApi.getTransport(id);
    }

    public ResponseEntity<TransportResponseDTO> getTransportFallback(Long id, Throwable t) {
        log.error("FALLBACK getTransport! transportId: {}, error: {}", id, t.getClass().getSimpleName());
        throw new RuntimeException("Transport service unavailable", t);
    }

    @CircuitBreaker(name = "transportService", fallbackMethod = "updateTransportStatusFallback")
    public ResponseEntity<TransportResponseDTO> updateTransportStatus(Long id, String status) {
        log.debug("Calling transport-service to update status: transportId={}, status={}", id, status);
        return transportServiceApi.updateTransportStatus(id, status);
    }

    public ResponseEntity<TransportResponseDTO> updateTransportStatusFallback(Long id, String status, Throwable t) {
        log.error("FALLBACK updateTransportStatus! transportId: {}, status: {}, error: {}",
                id, status, t.getClass().getSimpleName());
        throw new RuntimeException("Transport service unavailable", t);
    }

    @CircuitBreaker(name = "transportService", fallbackMethod = "updateTransportCoordinatesFallback")
    public ResponseEntity<Void> updateTransportCoordinates(UpdateCoordinatesDTO dto) {
        log.debug("Calling transport-service to update coordinates: transportId={}", dto.transportId());
        return transportServiceApi.updateTransportCoordinates(dto);
    }

    public ResponseEntity<Void> updateTransportCoordinatesFallback(UpdateCoordinatesDTO dto, Throwable t) {
        log.error("FALLBACK updateTransportCoordinates! transportId: {}, error: {}",
                dto.transportId(), t.getClass().getSimpleName());
        throw new RuntimeException("Transport service unavailable", t);
    }
}