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
import reactor.core.publisher.Mono;

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
    public Mono<ResponseEntity<RentalResponseDTO>> startRental(
            @Valid @RequestBody StartRentalRequestDTO request
    ) {
        // Get current user by email
        // You'll need to get userId from UserService
        // For now, assuming userId is passed in request

        return rentalService.startRental(
                request.userId(), // This should come from auth context
                request.transportId(),
                request.startLatitude(),
                request.startLongitude()
        ).map(rental -> ResponseEntity.status(HttpStatus.CREATED).body(rental));
    }

    /**
     * End current active rental
     */
    @PostMapping("/end")
    public Mono<ResponseEntity<RentalResponseDTO>> endRental(
            @Valid @RequestBody EndRentalRequestDTO request
    ) {
        // Get userId from auth context
        return rentalService.endRental(
                request.userId(), // This should come from auth context
                request.endLatitude(),
                request.endLongitude()
        ).map(ResponseEntity::ok);
    }

    /**
     * Cancel current active rental
     */
    @PostMapping("/cancel")
    public Mono<ResponseEntity<Void>> cancelRental(
            @RequestParam Long userId // Should come from auth context
    ) {
        return rentalService.cancelRental(userId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    /**
     * Get user's current active rental
     */
    @GetMapping("/active")
    public Mono<ResponseEntity<RentalResponseDTO>> getActiveRental(@RequestParam Long userId) {
        return rentalService.getActiveRental(userId)
                .map(ResponseEntity::ok);
    }

    /**
     * Get user's rental history with pagination
     */
    @GetMapping("/history")
    public Mono<ResponseEntity<PageResponseDTO<RentalResponseDTO>>> getRentalHistory(
            @RequestParam Long userId, // Should come from auth context
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size
    ) {
        return rentalService.getUserRentalHistory(userId, page, size)
                .map(ResponseEntity::ok);    }
}

