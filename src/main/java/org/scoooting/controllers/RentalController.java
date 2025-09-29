package org.scoooting.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.scoooting.dto.common.PageResponseDTO;
import org.scoooting.dto.request.EndRentalRequestDTO;
import org.scoooting.dto.request.StartRentalRequestDTO;
import org.scoooting.dto.response.RentalResponseDTO;
import org.scoooting.services.RentalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RentalResponseDTO> startRental(
            @Valid @RequestBody StartRentalRequestDTO request,
            Authentication auth
    ) {
        // Get current user by email
        String email = auth.getName();
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
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RentalResponseDTO> endRental(
            @Valid @RequestBody EndRentalRequestDTO request,
            Authentication auth
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
    @PreAuthorize("hasRole('USER')")
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
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RentalResponseDTO> getActiveRental(
            @RequestParam Long userId // Should come from auth context
    ) {
        Optional<RentalResponseDTO> activeRental = rentalService.getActiveRental(userId);
        return ResponseEntity.ok(activeRental.orElse(null));
    }

    /**
     * Get user's rental history with pagination
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PageResponseDTO<RentalResponseDTO>> getRentalHistory(
            @RequestParam Long userId, // Should come from auth context
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size
    ) {
        PageResponseDTO<RentalResponseDTO> result = rentalService.getUserRentalHistory(userId, page, size);
        return ResponseEntity.ok(result);
    }
}

