package org.scoooting.transport.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.scoooting.transport.dto.request.UpdateCoordinatesDTO;
import org.scoooting.transport.dto.response.TransportResponseDTO;
import org.scoooting.transport.entities.enums.TransportType;
import org.scoooting.transport.services.TransportService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<List<TransportResponseDTO>> findNearestTransports(
            @RequestParam @DecimalMin("-90") @DecimalMax("90") Double lat,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") Double lng,
            @RequestParam(defaultValue = "2.0") @DecimalMin("0.1") @DecimalMax("50") Double radiusKm
    ) {
        List<TransportResponseDTO> transports = transportService.findNearestTransports(lat, lng, radiusKm);
        return ResponseEntity.ok(transports);
    }

    /**
     * Get the nearest transports by specific type
     */
    @GetMapping("/nearest/{type}")
    public ResponseEntity<List<TransportResponseDTO>> findNearestTransportsByType(
            @PathVariable TransportType type,
            @RequestParam @DecimalMin("-90") @DecimalMax("90") Double lat,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") Double lng,
            @RequestParam(defaultValue = "2.0") @DecimalMin("0.1") @DecimalMax("50") Double radiusKm
    ) {
        List<TransportResponseDTO> transports = transportService.findTransportsByType(type, lat, lng, radiusKm);
        return ResponseEntity.ok(transports);
    }

    /**
     * Get transport status id
     */
    @GetMapping("/status/{name}")
    public ResponseEntity<Long> getTransportStatusId(@PathVariable String name) {
        return ResponseEntity.ok(transportService.getStatusId(name));
    }

    /**
     * Get specific transport details
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransportResponseDTO> getTransport(@PathVariable Long id) {
        TransportResponseDTO transport = transportService.getTransportById(id);
        return ResponseEntity.ok(transport);
    }

    /**
     * Get all available transports by type
     */
    @GetMapping("/available/{type}")
    public ResponseEntity<List<TransportResponseDTO>> findAvailableTransportsByType(
            @PathVariable TransportType type
    ) {
        List<TransportResponseDTO> transports = transportService.findAvailableTransportsByType(type);
        return ResponseEntity.ok(transports);
    }

    /**
     * Get availability statistics for all transport types
     */
    @GetMapping("/stats/availability")
    public ResponseEntity<Map<String, Long>> getAvailabilityStats() {
        Map<String, Long> stats = transportService.getAvailabilityStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Update transport status (ADMIN only)
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<TransportResponseDTO> updateTransportStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        TransportResponseDTO transport = transportService.updateTransportStatus(id, status);
        return ResponseEntity.ok(transport);
    }

    @PutMapping("/update-coordinates")
    public ResponseEntity<Void> updateTransportCoordinates(@Valid @RequestBody UpdateCoordinatesDTO updateCoordinatesDTO) {
        transportService.updateCoordinates(updateCoordinatesDTO);
        return ResponseEntity.ok().build();
    }
}
