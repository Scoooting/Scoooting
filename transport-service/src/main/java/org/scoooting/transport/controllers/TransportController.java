package org.scoooting.transport.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.scoooting.transport.dto.request.UpdateCoordinatesDTO;
import org.scoooting.transport.dto.response.TransportResponseDTO;
import org.scoooting.transport.entities.enums.TransportType;
import org.scoooting.transport.services.TransportService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transports")
@Validated
public class TransportController {

    private final TransportService transportService;

    /**
     * Get all available transports within specified radius
     */
    @GetMapping("/nearest")
    public Flux<TransportResponseDTO> findNearestTransports(
            @RequestParam @DecimalMin("-90") @DecimalMax("90") Double lat,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") Double lng,
            @RequestParam(defaultValue = "2.0") @DecimalMin("0.1") @DecimalMax("50") Double radiusKm
    ) {
        return transportService.findNearestTransports(lat, lng, radiusKm);
    }

    /**
     * Get the nearest transports by specific type
     */
    @GetMapping("/nearest/{type}")
    public Flux<TransportResponseDTO> findNearestTransportsByType(
            @PathVariable TransportType type,
            @RequestParam @DecimalMin("-90") @DecimalMax("90") Double lat,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") Double lng,
            @RequestParam(defaultValue = "2.0") @DecimalMin("0.1") @DecimalMax("50") Double radiusKm
    ) {
        return transportService.findTransportsByType(type, lat, lng, radiusKm);
    }

    /**
     * Get transport status id
     */
    @GetMapping("/status/{name}")
    public Mono<Long> getTransportStatusId(@PathVariable String name) {
        return transportService.getStatusId(name);
    }

    /**
     * Get specific transport details
     */
    @GetMapping("/{id}")
    public Mono<TransportResponseDTO> getTransport(@PathVariable Long id) {
        return transportService.getTransportById(id);
    }

    /**
     * Get all available transports by type
     */
    @GetMapping("/available/{type}")
    public Flux<TransportResponseDTO> findAvailableTransportsByType(
            @PathVariable TransportType type
    ) {
        return transportService.findAvailableTransportsByType(type);
    }

    /**
     * Get availability statistics for all transport types
     */
    @GetMapping("/stats/availability")
    public Mono<Map<String, Long>> getAvailabilityStats() {
        return transportService.getAvailabilityStats();    }

    /**
     * Update transport status (ADMIN only)
     */
    @PutMapping("/{id}/status")
    public Mono<TransportResponseDTO> updateTransportStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return transportService.updateTransportStatus(id, status);
    }

    @PutMapping("/update-coordinates")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> updateCoordinates(@Valid @RequestBody UpdateCoordinatesDTO dto) {
        return transportService.updateCoordinates(dto);
    }
}
