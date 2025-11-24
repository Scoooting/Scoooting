package org.scoooting.rental.clients.resilient;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.rental.clients.feign.FeignTransportClient;
import org.scoooting.rental.dto.UpdateCoordinatesDTO;
import org.scoooting.rental.dto.response.TransportResponseDTO;
import org.scoooting.rental.exceptions.TransportNotFoundException;
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
            log.error("Transport status '{}' not found", name);
            throw new TransportNotFoundException("Transport status '" + name + "' not found");
        } catch (FeignException e) {
            log.error("Transport service unavailable: {}", e.getMessage());
            throw new TransportServiceException("Transport service is currently unavailable");
        }
    }

    public ResponseEntity<Long> getTransportStatusIdFallback(String name, Throwable t) {
        log.error("FALLBACK getTransportStatusId! status: {}, error: {}", name, t.getClass().getSimpleName());

        if (t instanceof TransportNotFoundException) {
            throw (TransportNotFoundException) t;
        }

        if (t instanceof TransportServiceException) {
            throw (TransportServiceException) t;
        }

        throw new TransportServiceException("Transport service unavailable", t);
    }
    @CircuitBreaker(name = "transportService", fallbackMethod = "getTransportFallback")
    public ResponseEntity<TransportResponseDTO> getTransport(Long id) {
        log.debug("Calling transport-service for transportId: {}", id);
        try {
            return null;
        } catch (FeignException.NotFound e) {
            log.error("Transport {} not found in transport-service", id);
            throw new TransportNotFoundException("Transport with ID " + id + " not found");
        } catch (FeignException e) {
            log.error("Transport service unavailable: {}", e.getMessage());
            throw new TransportServiceException("Transport service is currently unavailable");
        }
    }

    public ResponseEntity<TransportResponseDTO> getTransportFallback(Long id, Throwable t) {
        log.error("FALLBACK getTransport! transportId: {}, error: {}", id, t.getClass().getSimpleName());

        if (t instanceof TransportNotFoundException) {
            throw (TransportNotFoundException) t;
        }

        if (t instanceof TransportServiceException) {
            throw (TransportServiceException) t;
        }

        throw new TransportServiceException("Transport service unavailable", t);
    }

    @CircuitBreaker(name = "transportService", fallbackMethod = "updateTransportStatusFallback")
    public ResponseEntity<TransportResponseDTO> updateTransportStatus(Long id, String status) {
        log.debug("Calling transport-service to update status: transportId={}, status={}", id, status);
        try {
            return null;
        } catch (FeignException.NotFound e) {
            log.error("Transport {} not found for status update", id);
            throw new TransportNotFoundException("Transport with ID " + id + " not found");
        } catch (FeignException e) {
            log.error("Transport service unavailable: {}", e.getMessage());
            throw new TransportServiceException("Transport service is currently unavailable");
        }
    }

    public ResponseEntity<TransportResponseDTO> updateTransportStatusFallback(Long id, String status, Throwable t) {
        log.error("FALLBACK updateTransportStatus! transportId: {}, status: {}, error: {}",
                id, status, t.getClass().getSimpleName());

        if (t instanceof TransportNotFoundException) {
            throw (TransportNotFoundException) t;
        }

        if (t instanceof TransportServiceException) {
            throw (TransportServiceException) t;
        }

        throw new TransportServiceException("Transport service unavailable", t);
    }

    @CircuitBreaker(name = "transportService", fallbackMethod = "updateTransportCoordinatesFallback")
    public ResponseEntity<Void> updateTransportCoordinates(UpdateCoordinatesDTO dto) {
        log.debug("Calling transport-service to update coordinates: transportId={}", dto.transportId());
        try {
            return null;
        } catch (FeignException.NotFound e) {
            log.error("Transport {} not found for coordinates update", dto.transportId());
            throw new TransportNotFoundException("Transport with ID " + dto.transportId() + " not found");
        } catch (FeignException e) {
            log.error("Transport service unavailable: {}", e.getMessage());
            throw new TransportServiceException("Transport service is currently unavailable");
        }
    }

    public ResponseEntity<Void> updateTransportCoordinatesFallback(UpdateCoordinatesDTO dto, Throwable t) {
        log.error("FALLBACK updateTransportCoordinates! transportId: {}, error: {}",
                dto.transportId(), t.getClass().getSimpleName());

        if (t instanceof TransportNotFoundException) {
            throw (TransportNotFoundException) t;
        }

        if (t instanceof TransportServiceException) {
            throw (TransportServiceException) t;
        }

        throw new TransportServiceException("Transport service unavailable", t);
    }
}