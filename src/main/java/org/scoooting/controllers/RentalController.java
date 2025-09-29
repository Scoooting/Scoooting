package org.scoooting.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.dto.common.PageResponseDTO;
import org.scoooting.dto.request.EndRentalRequestDTO;
import org.scoooting.dto.request.StartRentalRequestDTO;
import org.scoooting.dto.response.RentalResponseDTO;
import org.scoooting.dto.response.UserResponseDTO;
import org.scoooting.services.RentalService;
import org.scoooting.services.UserService;
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
@Slf4j
public class RentalController {

    private final RentalService rentalService;
    private final UserService userService;

    /**
     * Start a new rental
     */
    @PostMapping("/start")
    public ResponseEntity<RentalResponseDTO> startRental(
            @Valid @RequestBody StartRentalRequestDTO request,
            Authentication auth
    ) {
        log.info("Received request: {}", request);
        log.info("Auth: {}", auth.getName());

        // Get current user by email
        String email = auth.getName();
        UserResponseDTO user = userService.findUserByEmail(email);

        RentalResponseDTO rental = rentalService.startRental(
                user.id(),
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
            @Valid @RequestBody EndRentalRequestDTO request,
            Authentication auth
    ) {
        String email = auth.getName();
        UserResponseDTO user = userService.findUserByEmail(email);
        RentalResponseDTO rental = rentalService.endRental(
                user.id(),
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
            Authentication auth
    ) {
        String email = auth.getName();
        UserResponseDTO user = userService.findUserByEmail(email);
        rentalService.cancelRental(user.id());
        return ResponseEntity.noContent().build();
    }

    /**
     * Get user's current active rental
     */
    @GetMapping("/active")
    public ResponseEntity<RentalResponseDTO> getActiveRental(
            Authentication auth
    ) {
        String email = auth.getName();
        UserResponseDTO user = userService.findUserByEmail(email);
        Optional<RentalResponseDTO> activeRental = rentalService.getActiveRental(user.id());
        return ResponseEntity.ok(activeRental.orElse(null));
    }

    /**
     * Get user's rental history with pagination
     */
    @GetMapping("/history")
    public ResponseEntity<PageResponseDTO<RentalResponseDTO>> getRentalHistory(
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size,
            Authentication auth
    ) {
        String email = auth.getName();
        UserResponseDTO user = userService.findUserByEmail(email);
        PageResponseDTO<RentalResponseDTO> result = rentalService.getUserRentalHistory(user.id(), page, size);
        return ResponseEntity.ok(result);
    }
}

