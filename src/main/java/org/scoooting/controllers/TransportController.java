package org.scoooting.controllers;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.scoooting.dto.response.TransportResponseDTO;
import org.scoooting.entities.enums.TransportType;
import org.scoooting.services.TransportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
     * Get nearest transports by specific type
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

    @GetMapping
    public ResponseEntity<List<TransportResponseDTO>> getAllTransports(
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer size
    ) {
        List<TransportResponseDTO> transports = transportService.findAllTransports(page, size);
        // Without X-Total-Count header - infinite scrolling
        return ResponseEntity.ok(transports);
    }
}
