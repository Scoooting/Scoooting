package org.scoooting.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.scoooting.dto.*;
import org.scoooting.repositories.ScooterRepository;
import org.scoooting.repositories.UserRepository;
import org.scoooting.services.RentalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/rentals")
public class RentalController {

    private final RentalService rentalService;
    private final ScooterRepository scooterRepository;
    private final UserRepository userRepository;

    /**
     * Start a new scooter rental
     */
    @PostMapping("/start")
    public ResponseEntity<RentalDTO> startRental(
            @RequestParam Long userId,
            @RequestBody @Valid StartRentalRequest request) {

        RentalDTO rental = rentalService.startRental(
                userId,
                request.transportId(),
                request.startLatitude(),
                request.startLongitude()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(rental);
    }

    /**
     * End current active rental
     */
    @PostMapping("/end")
    public ResponseEntity<RentalDTO> endRental(
            @RequestParam Long userId,
            @RequestBody @Valid EndRentalRequest request) {

        RentalDTO rental = rentalService.endRental(
                userId,
                request.endLatitude(),
                request.endLongitude()
        );

        return ResponseEntity.ok(rental);
    }

    /**
     * Cancel current active rental
     */
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelRental(@RequestParam Long userId) {
        rentalService.cancelRental(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get user's current active rental
     */
    @GetMapping("/active")
    public ResponseEntity<RentalDTO> getActiveRental(@RequestParam Long userId) {
        Optional<RentalDTO> activeRental = rentalService.getActiveRental(userId);

        // Return 200 with null body if no active rental
        return ResponseEntity.ok(activeRental.orElse(null));
    }
    /**
     * Get user's rental history with pagination
     */
    @GetMapping("/history")
    public ResponseEntity<List<RentalDTO>> getRentalHistory(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {

        PaginatedRentalsDTO result = rentalService.getUserRentalHistory(userId, offset, limit);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.totalCount()))
                .body(result.rentals());
    }

    /**
     * COMPLEX QUERY 1: Get top users analytics
     * For ADMIN and ANALYST roles - business intelligence endpoint
     */
    @PostMapping("/analytics/top-users")
    public ResponseEntity<List<UserAnalyticsDTO>> getTopUsersByUsage(
            @RequestBody @Valid AnalyticsRequest request) {

        try {
            List<UserAnalyticsDTO> analytics = rentalService.getTopUsersByUsage(
                    request.startDate(),
                    request.endDate(),
                    request.minRentals() != null ? request.minRentals(): 1,
                    request.limit() != null ? request.limit() : 50
            );

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            System.err.println("Analytics error: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * COMPLEX QUERY 2: Get scooter utilization analytics in specific area
     * For OPERATOR and ADMIN roles - fleet management endpoint
     */
    @PostMapping("/analytics/scooter-utilization")
    public ResponseEntity<List<TransportAnalyticsDTO>> getScooterUtilizationInArea(
            @RequestBody @Valid AreaAnalyticsRequest request) {

        try {
            List<TransportAnalyticsDTO> analytics = rentalService.getTransportUtilizationInArea(
                    request.centerLatitude(),
                    request.centerLongitude(),
                    request.radiusMeters(),
                    request.startDate()
            );

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            System.err.println("Transport analytics error: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get revenue analytics by city
     * For business intelligence and financial reporting
     */
//    @GetMapping("/analytics/revenue-by-city")
//    public ResponseEntity<List<CityRevenueDTO>> getRevenueByCity(
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
//
//        List<CityRevenueDTO> revenue = rentalService.getRevenueByCity(startDate, endDate);
//        return ResponseEntity.ok(revenue);
//    }

    /**
     * Get scooters needing maintenance based on usage
     * For OPERATOR role - maintenance management
     */
    @GetMapping("/maintenance/alerts")
    public ResponseEntity<List<MaintenanceAlertDTO>> getMaintenanceAlerts(
            @RequestParam(defaultValue = "50") int maxRentals,
            @RequestParam(defaultValue = "3000") int maxMinutes) {

        List<MaintenanceAlertDTO> alerts = rentalService.getTransportsNeedingMaintenance(
                maxRentals, maxMinutes);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Bulk operations for multiple scooter rentals (future feature)
     * Start rental for multiple scooters (e.g., group rental)
     */
    @PostMapping("/bulk/start")
    public ResponseEntity<List<RentalDTO>> startBulkRental(
            @RequestParam Long userId,
            @RequestBody List<StartRentalRequest> requests) {

        List<RentalDTO> rentals = new ArrayList<>();

        for (StartRentalRequest request : requests) {
            try {
                RentalDTO rental = rentalService.startRental(
                        userId,
                        request.transportId(),
                        request.startLatitude(),
                        request.startLongitude()
                );
                rentals.add(rental);
            } catch (Exception e) {
                // Log error but continue with other rentals
                // In production, you'd want more sophisticated error handling
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(rentals);
    }

    /**
     * Exception handling for rental-specific errors
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
