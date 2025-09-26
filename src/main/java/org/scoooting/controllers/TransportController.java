package org.scoooting.controllers;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.TransportDTO;
import org.scoooting.entities.enums.TransportType;
import org.scoooting.services.TransportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transports")
public class TransportController {

    private final TransportService transportService;

    /**
     * Get all available transports within 2km radius
     */
    @GetMapping("/nearest")
    public ResponseEntity<?> findNearestTransports(
            @RequestParam float lat,
            @RequestParam float lon) {
        return ResponseEntity.ok(transportService.findNearestTransports(lat, lon));
    }

    /**
     * Get the nearest transports by specific type
     */
    @GetMapping("/nearest/{type}")
    public ResponseEntity<?> findNearestTransportsByType(
            @PathVariable TransportType type,
            @RequestParam float lat,
            @RequestParam float lon,
            @RequestParam(defaultValue = "2000") int radius) {
        return ResponseEntity.ok(transportService.findNearestTransportsByType(lat, lon, radius, type));
    }

    /**
     * Get transports in specific city with pagination
     */
    @GetMapping("/city")
    public ResponseEntity<?> findTransportsInCity(
            @RequestParam String city,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(transportService.findTransportsInCity(city, offset, limit));
    }

    /**
     * Get all available transports by type
     */
    @GetMapping("/available/{type}")
    public ResponseEntity<?> findAvailableTransportsByType(@PathVariable TransportType type) {
        return ResponseEntity.ok(transportService.findAvailableTransportsByType(type));
    }

    /**
     * Get availability statistics for all transport types
     */
    @GetMapping("/stats/availability")
    public ResponseEntity<?> getAvailabilityStats() {
        return ResponseEntity.ok(transportService.getAvailabilityStats());
    }

    /**
     * Get specific transport details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTransport(@PathVariable Long id, @RequestParam TransportType type) {
        return ResponseEntity.ok(transportService.getTransportById(id, type));
    }

}
