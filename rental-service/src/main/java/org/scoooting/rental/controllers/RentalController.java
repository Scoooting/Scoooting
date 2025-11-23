package org.scoooting.rental.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.rental.config.UserPrincipal;
import org.scoooting.rental.dto.common.PageResponseDTO;
import org.scoooting.rental.dto.request.EndRentalRequestDTO;
import org.scoooting.rental.dto.request.StartRentalRequestDTO;
import org.scoooting.rental.dto.response.RentalResponseDTO;
import org.scoooting.rental.services.RentalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rentals")
@Validated
public class RentalController {

    private final RentalService rentalService;

    // ==================== USER OPERATIONS ====================

    @Operation(
            summary = "[USER] Start a new rental",
            description = "User can start renting available transport. Available to: ALL authenticated users",
            tags = {"User Rental Operations"}
    )
    @PostMapping("/start")
    public Mono<ResponseEntity<RentalResponseDTO>> startRental(
            @Valid @RequestBody StartRentalRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("User {} starting rental for transport {}", principal.getUserId(), request.transportId());

        return rentalService.startRental(
                principal.getUserId(),
                request.transportId(),
                request.startLatitude(),
                request.startLongitude()
        ).map(rental -> ResponseEntity.status(HttpStatus.CREATED).body(rental));
    }

    @Operation(
            summary = "[USER] End current rental",
            description = "User can end their own active rental. Available to: ALL authenticated users",
            tags = {"User Rental Operations"}
    )
    @PostMapping("/end")
    public Mono<ResponseEntity<RentalResponseDTO>> endRental(
            @Valid @RequestBody EndRentalRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("User {} ending their rental", principal.getUserId());

        return rentalService.endRental(
                principal.getUserId(),
                request.endLatitude(),
                request.endLongitude()
        ).map(ResponseEntity::ok);
    }

    @Operation(
            summary = "[USER] Cancel current rental",
            description = "User can cancel their own active rental. Available to: ALL authenticated users",
            tags = {"User Rental Operations"}
    )
    @PostMapping("/cancel")
    public Mono<ResponseEntity<Void>> cancelRental(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("User {} cancelling their rental", principal.getUserId());

        return rentalService.cancelRental(principal.getUserId())
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Operation(
            summary = "[USER] Get my active rental",
            description = "Get current user's active rental. Available to: ALL authenticated users",
            tags = {"User Rental Operations"}
    )
    @GetMapping("/active")
    public Mono<ResponseEntity<RentalResponseDTO>> getActiveRental(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("User {} fetching their active rental", principal.getUserId());

        return rentalService.getActiveRental(principal.getUserId())
                .map(ResponseEntity::ok);
    }

    @Operation(
            summary = "[USER] Get my rental history",
            description = "Get current user's past rentals. Available to: ALL authenticated users",
            tags = {"User Rental Operations"}
    )
    @GetMapping("/history")
    public Mono<ResponseEntity<PageResponseDTO<RentalResponseDTO>>> getRentalHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer size
    ) {
        log.info("User {} fetching rental history", principal.getUserId());

        return rentalService.getUserRentalHistory(principal.getUserId(), page, size)
                .map(ResponseEntity::ok);
    }

    // ==================== SUPPORT OPERATIONS ====================

    @Operation(
            summary = "[SUPPORT] Force end any rental",
            description = "Support can forcefully end any user's rental (e.g., customer complaint). Available to: SUPPORT, ADMIN",
            tags = {"Support Rental Operations"}
    )
    @PostMapping("/{rentalId}/force-end")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN')")
    public Mono<ResponseEntity<RentalResponseDTO>> forceEndRental(
            @PathVariable Long rentalId,
            @RequestParam Double endLatitude,
            @RequestParam Double endLongitude,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Support {} force-ending rental {}", principal.getEmail(), rentalId);

        return rentalService.forceEndRental(rentalId, endLatitude, endLongitude)
                .map(ResponseEntity::ok);
    }

    // ==================== ANALYST OPERATIONS ====================

    @Operation(
            summary = "[ANALYST] Get all rentals",
            description = "View all system rentals for analysis. Available to: ANALYST, ADMIN",
            tags = {"Analyst Rental Operations"}
    )
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public Mono<ResponseEntity<PageResponseDTO<RentalResponseDTO>>> getAllRentals(
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Analyst {} fetching all rentals", principal.getEmail());

        return rentalService.getAllRentals(page, size)
                .map(ResponseEntity::ok);
    }
}