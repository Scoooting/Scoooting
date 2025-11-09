package org.scoooting.transport.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.scoooting.transport.dto.request.UpdateCoordinatesDTO;
import org.scoooting.transport.dto.response.ScrollResponseDTO;
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
     * Get specific transport details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get transport by ID",
            description = "Returns transport details including status and location")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transport found"),
            @ApiResponse(responseCode = "404", description = "Transport not found"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    public Mono<TransportResponseDTO> getTransport(@PathVariable Long id) {
        return transportService.getTransportById(id);
    }

    /**
     * Get all available transports by type with "infinite scrolling"
     *
     * @param type
     * @param page
     * @param size
     * @return available transport with hasMore flag for infinite scrolling
     */
    @GetMapping("/available/{type}")
    @Operation(summary = "Scroll available transports by type",
            description = "Infinite scroll pagination without total count (faster)")
    public Mono<ScrollResponseDTO<TransportResponseDTO>> scrollAvailableTransportsByType(
            @PathVariable TransportType type,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer size
    ) {
        return transportService.scrollAvailableTransportsByType(type, page, size);
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
    @Operation(summary = "Update transport coordinates",
            description = "Updates latitude and longitude of a transport")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coordinates updated successfully"),
            @ApiResponse(responseCode = "404", description = "Transport not found"),
            @ApiResponse(responseCode = "400", description = "Invalid coordinates")
    })
    public Mono<TransportResponseDTO> updateCoordinates(
            @Valid @RequestBody UpdateCoordinatesDTO dto
    ) {
        return transportService.updateCoordinates(dto);
    }
}
