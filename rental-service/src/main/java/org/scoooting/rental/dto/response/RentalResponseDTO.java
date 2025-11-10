package org.scoooting.rental.dto.response;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record RentalResponseDTO(
        @NotNull Long id,
        @NotNull Long userId,
        @NotNull Long transportId,
        @NotNull Instant startTime,
        Instant endTime,
        BigDecimal totalCost,
        Integer durationMinutes
) {}
