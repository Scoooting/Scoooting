package org.scoooting.rental.clients.resilient;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.rental.clients.feign.FeignTransportClient;
import org.scoooting.rental.dto.UpdateCoordinatesDTO;
import org.scoooting.rental.dto.response.TransportResponseDTO;
import org.scoooting.rental.exceptions.TransportServiceException;
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
        try {
            return transportServiceApi.getTransportStatusId(name);
        } catch (FeignException.NotFound e) {
            throw new TransportServiceException("Transport status '" + name + "' not found");
        }
    }

    public ResponseEntity<Long> getTransportStatusIdFallback(String name, Throwable t) {
        log.error("FALLBACK getTransportStatusId! status: {}, error: {}", name, t.getClass().getSimpleName());
        throw new TransportServiceException("Transport service unavailable", t);
    }

    @CircuitBreaker(name = "transportService", fallbackMethod = "getTransportFallback")
    public ResponseEntity<TransportResponseDTO> getTransport(Long id) {
        log.debug("Calling transport-service for transportId: {}", id);
        try {
            return transportServiceApi.getTransport(id);
        } catch (FeignException.NotFound e) {
            throw new TransportServiceException("Transport with ID " + id + " not found");
        }
    }

    public ResponseEntity<TransportResponseDTO> getTransportFallback(Long id, Throwable t) {
        log.error("FALLBACK getTransport! transportId: {}, error: {}", id, t.getClass().getSimpleName());

        // ✅ Если это не 404, значит сервис недоступен
        if (!(t instanceof TransportServiceException && t.getMessage().contains("not found"))) {
            throw new TransportServiceException("Transport service unavailable", t);
        }
        throw (TransportServiceException) t;
    }

    @CircuitBreaker(name = "transportService", fallbackMethod = "updateTransportStatusFallback")
    public ResponseEntity<TransportResponseDTO> updateTransportStatus(Long id, String status) {
        log.debug("Calling transport-service to update status: transportId={}, status={}", id, status);
        try {
            return transportServiceApi.updateTransportStatus(id, status);
        } catch (FeignException.NotFound e) {
            throw new TransportServiceException("Transport with ID " + id + " not found");
        }
    }

    public ResponseEntity<TransportResponseDTO> updateTransportStatusFallback(Long id, String status, Throwable t) {
        log.error("FALLBACK updateTransportStatus! transportId: {}, status: {}, error: {}",
                id, status, t.getClass().getSimpleName());

        if (!(t instanceof TransportServiceException && t.getMessage().contains("not found"))) {
            throw new TransportServiceException("Transport service unavailable", t);
        }
        throw (TransportServiceException) t;
    }

    @CircuitBreaker(name = "transportService", fallbackMethod = "updateTransportCoordinatesFallback")
    public ResponseEntity<Void> updateTransportCoordinates(UpdateCoordinatesDTO dto) {
        log.debug("Calling transport-service to update coordinates: transportId={}", dto.transportId());
        return transportServiceApi.updateTransportCoordinates(dto);
    }

    public ResponseEntity<Void> updateTransportCoordinatesFallback(UpdateCoordinatesDTO dto, Throwable t) {
        log.error("FALLBACK updateTransportCoordinates! transportId: {}, error: {}",
                dto.transportId(), t.getClass().getSimpleName());
        throw new TransportServiceException("Transport service unavailable", t);
    }
}