package org.scoooting.rental.adapters.web.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.rental.adapters.security.UserPrincipal;
import org.scoooting.rental.application.usecase.rentals.*;
import org.scoooting.rental.application.usecase.sendings.SendNotificationUseCase;
import org.scoooting.rental.application.usecase.sendings.SendReportUseCase;
import org.scoooting.rental.application.dto.PageResponseDTO;
import org.scoooting.rental.adapters.message.kafka.dto.RentalEventDto;
import org.scoooting.rental.adapters.web.dto.EndRentalRequestDTO;
import org.scoooting.rental.adapters.web.dto.StartRentalRequestDTO;
import org.scoooting.rental.application.dto.RentalResponseDTO;
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

    private final StartRentalUseCase startRentalUseCase;
    private final EndRentalUseCase endRentalUseCase;
    private final CancelRentalUseCase cancelRentalUseCase;
    private final ForceEndRentalUseCase forceEndRentalUseCase;
    private final GetActiveRentalUseCase getActiveRentalUseCase;
    private final RentalHistoryUseCase rentalHistoryUseCase;

    private final SendReportUseCase sendReportUseCase;
    private final SendNotificationUseCase sendNotificationUseCase;

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

        return startRentalUseCase.startRental(
                principal.getUserId(),
                request.transportId(),
                request.startLatitude(),
                request.startLongitude()
        ).flatMap(rental -> sendNotificationUseCase.sendNotification(new RentalEventDto(
                principal.getUserId(),
                RentalEventDto.RentalType.START))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(rental)));
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

        return endRentalUseCase.endRental(
                principal.getUserId(),
                request.endLatitude(),
                request.endLongitude()
        ).flatMap(rental ->
                sendReportUseCase.sendReport(rental, principal)
                        .then(sendNotificationUseCase.sendNotification(new RentalEventDto(
                                principal.getUserId(),
                                RentalEventDto.RentalType.END)
                ))
                .thenReturn(ResponseEntity.ok(rental)));
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

        return cancelRentalUseCase.cancelRental(principal.getUserId())
                .flatMap(rental ->
                        sendReportUseCase.sendReport(rental, principal)
                                .then(sendNotificationUseCase.sendNotification(new RentalEventDto(
                                        principal.getUserId(),
                                        RentalEventDto.RentalType.CANCEL)
                                ))
                                .thenReturn(ResponseEntity.ok().build()));
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

        return getActiveRentalUseCase.getActiveRental(principal.getUserId())
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

        return rentalHistoryUseCase.getUserRentalHistory(principal.getUserId(), page, size)
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

        return forceEndRentalUseCase.forceEndRental(rentalId, endLatitude, endLongitude)
                .flatMap(rental ->
                        sendReportUseCase.sendReport(rental.rentalResponseDTO(), rental.userPrincipal())
                                .then(sendNotificationUseCase.sendNotification(new RentalEventDto(
                                        rental.userPrincipal().getUserId(),
                                        RentalEventDto.RentalType.FORCE_END)
                                ))
                                .thenReturn(ResponseEntity.ok().build()));
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

        return getActiveRentalUseCase.getAllRentals(page, size)
                .map(ResponseEntity::ok);
    }
}