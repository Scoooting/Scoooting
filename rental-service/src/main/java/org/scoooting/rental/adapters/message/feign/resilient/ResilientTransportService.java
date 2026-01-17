package org.scoooting.rental.adapters.message.feign.resilient;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.rental.adapters.message.feign.FeignTransportClient;
import org.scoooting.rental.application.dto.TransportResponseDTO;
import org.scoooting.rental.application.ports.TransportClient;
import org.scoooting.rental.domain.exceptions.TransportNotFoundException;
import org.scoooting.rental.domain.exceptions.TransportServiceException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResilientTransportService implements TransportClient {

    private final FeignTransportClient transportServiceApi;

    @CircuitBreaker(name = "transportService", fallbackMethod = "getTransportStatusIdFallback")
    public Long getTransportStatusId(String name) {
        log.debug("Calling transport-service for status: {}", name);
        try {
            return transportServiceApi.getTransportStatusId(name).getBody();
        } catch (FeignException.NotFound e) {
            log.error("Transport status '{}' not found", name);
            throw new TransportNotFoundException("Transport status '" + name + "' not found");
        } catch (FeignException e) {
            log.error("Transport service unavailable: {}", e.getMessage());
            throw new TransportServiceException("Transport service is currently unavailable");
        }
    }

    public Long getTransportStatusIdFallback(String name, Throwable t) {
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
    public TransportResponseDTO getTransport(Long id) {
        log.debug("Calling transport-service for transportId: {}", id);
        try {
            return transportServiceApi.getTransport(id).getBody();
        } catch (FeignException.NotFound e) {
            log.error("Transport {} not found in transport-service", id);
            throw new TransportNotFoundException("Transport with ID " + id + " not found");
        } catch (FeignException e) {
            log.error("Transport service unavailable: {}", e.getMessage());
            throw new TransportServiceException("Transport service is currently unavailable");
        }
    }

    public TransportResponseDTO getTransportFallback(Long id, Throwable t) {
        log.error("FALLBACK getTransport! transportId: {}, error: {}", id, t.getClass().getSimpleName());

        if (t instanceof TransportNotFoundException) {
            throw (TransportNotFoundException) t;
        }

        if (t instanceof TransportServiceException) {
            throw (TransportServiceException) t;
        }

        throw new TransportServiceException("Transport service unavailable", t);
    }
}