package org.scoooting.rental.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.scoooting.rental.dto.common.PageResponseDTO;
import org.scoooting.rental.dto.request.EndRentalRequestDTO;
import org.scoooting.rental.dto.request.StartRentalRequestDTO;
import org.scoooting.rental.dto.response.RentalResponseDTO;
import org.scoooting.rental.services.RentalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Просто нафиг закомментил всё, потому что контроллер очень жестко связан с закоменченным сервисом. Сори, за русский :).
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rentals")
@Validated
public class RentalController {

    private final RentalService rentalService;

    /**
     * Start a new rental
     */
    @PostMapping("/start")
    public ResponseEntity<RentalResponseDTO> startRental(
            @Valid @RequestBody StartRentalRequestDTO request
    ) {
        // Get current user by email
        // You'll need to get userId from UserService
        // For now, assuming userId is passed in request

        RentalResponseDTO rental = rentalService.startRental(
                request.userId(), // This should come from auth context
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
    public ResponseEntity<RentalResponseDTO> endRental(
            @Valid @RequestBody EndRentalRequestDTO request
    ) {
        // Get userId from auth context
        RentalResponseDTO rental = rentalService.endRental(
                request.userId(), // This should come from auth context
                request.endLatitude(),
                request.endLongitude()
        );

        return ResponseEntity.ok(rental);
    }

    /**
     * Cancel current active rental
     */
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelRental(
            @RequestParam Long userId // Should come from auth context
    ) {
        rentalService.cancelRental(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get user's current active rental
     */
    @GetMapping("/active")
    public ResponseEntity<RentalResponseDTO> getActiveRental(@RequestParam Long userId) {
        return ResponseEntity.ok(rentalService.getActiveRental(userId));
    }

    /**
     * Get user's rental history with pagination
     */
    @GetMapping("/history")
    public ResponseEntity<PageResponseDTO<RentalResponseDTO>> getRentalHistory(
            @RequestParam Long userId, // Should come from auth context
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size
    ) {
        PageResponseDTO<RentalResponseDTO> result = rentalService.getUserRentalHistory(userId, page, size);
        return ResponseEntity.ok(result);
    }
}

