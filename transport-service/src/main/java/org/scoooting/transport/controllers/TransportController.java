package org.scoooting.transport.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.transport.config.UserPrincipal;
import org.scoooting.transport.dto.request.UpdateCoordinatesDTO;
import org.scoooting.transport.dto.response.ScrollResponseDTO;
import org.scoooting.transport.dto.response.TransportResponseDTO;
import org.scoooting.transport.entities.enums.TransportType;
import org.scoooting.transport.services.TransportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transports")
@Validated
public class TransportController {

    private final TransportService transportService;

    // ==================== PUBLIC OPERATIONS ====================

    @Operation(
            summary = "[PUBLIC] Get nearest transports",
            description = "Find available transports within specified radius. Available to: Everyone",
            tags = {"User Operations"}
    )
    @GetMapping("/nearest")
    public Flux<TransportResponseDTO> findNearestTransports(
            @RequestParam @DecimalMin("-90") @DecimalMax("90") Double lat,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") Double lng,
            @RequestParam(defaultValue = "2.0") @DecimalMin("0.1") @DecimalMax("50") Double radiusKm
    ) {
        log.info("Finding nearest transports at ({}, {}) within {}km", lat, lng, radiusKm);
        return transportService.findNearestTransports(lat, lng, radiusKm);
    }

    @Operation(
            summary = "[PUBLIC] Get nearest transports by type",
            description = "Available to: Everyone",
            tags = {"User Operations"}
    )
    @GetMapping("/nearest/{type}")
    public Flux<TransportResponseDTO> findNearestTransportsByType(
            @PathVariable TransportType type,
            @RequestParam @DecimalMin("-90") @DecimalMax("90") Double lat,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") Double lng,
            @RequestParam(defaultValue = "2.0") @DecimalMin("0.1") @DecimalMax("50") Double radiusKm
    ) {
        log.info("Finding nearest {} at ({}, {}) within {}km", type, lat, lng, radiusKm);
        return transportService.findTransportsByType(type, lat, lng, radiusKm);
    }

    @Operation(
            summary = "[PUBLIC] Scroll available transports",
            description = "Infinite scroll pagination. Available to: Everyone",
            tags = {"User Operations"}
    )
    @GetMapping("/available/{type}")
    public Mono<ScrollResponseDTO<TransportResponseDTO>> scrollAvailableTransportsByType(
            @PathVariable TransportType type,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer size
    ) {
        log.info("Scrolling available transports of type: {}, page: {}, size: {}", type, page, size);
        return transportService.scrollAvailableTransportsByType(type, page, size);
    }

    @Operation(
            summary = "[PUBLIC] Get transport by ID",
            description = "Available to: Everyone",
            tags = {"User Operations"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transport found"),
            @ApiResponse(responseCode = "404", description = "Transport not found")
    })
    @GetMapping("/{id}")
    public Mono<TransportResponseDTO> getTransport(@PathVariable Long id) {
        log.info("Getting transport by id: {}", id);
        return transportService.getTransportById(id);
    }

    // ==================== ANALYST OPERATIONS ====================

    @Operation(
            summary = "[ANALYST] Get availability statistics",
            description = "Available to: ANALYST, ADMIN",
            tags = {"Analyst Operations"}
    )
    @GetMapping("/stats/availability")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public Mono<Map<String, Long>> getAvailabilityStats(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("User {} fetching availability stats", principal.getEmail());
        return transportService.getAvailabilityStats();
    }

    // ==================== OPERATOR OPERATIONS ====================

    @Operation(
            summary = "[OPERATOR] Update transport coordinates",
            description = "Operators can remotely update transport location (e.g., after maintenance). Available to: OPERATOR, ADMIN",
            tags = {"Operator Operations"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coordinates updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only operators can update coordinates"),
            @ApiResponse(responseCode = "404", description = "Transport not found")
    })
    @PutMapping("/update-coordinates")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public Mono<TransportResponseDTO> updateCoordinates(
            @Valid @RequestBody UpdateCoordinatesDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Operator {} updating coordinates for transport {}", principal.getEmail(), dto.transportId());
        return transportService.updateCoordinates(dto);
    }

    @Operation(
            summary = "[OPERATOR] Update transport status",
            description = "Change transport status (available, maintenance, broken, etc.). Available to: OPERATOR, ADMIN",
            tags = {"Operator Operations"}
    )
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public Mono<TransportResponseDTO> updateTransportStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Operator {} updating status of transport {} to {}", principal.getEmail(), id, status);
        return transportService.updateTransportStatus(id, status);
    }
}