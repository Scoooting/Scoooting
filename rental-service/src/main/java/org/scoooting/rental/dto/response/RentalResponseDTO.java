package org.scoooting.rental.dto.response;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RentalResponseDTO(
        @NotNull Long id,
        @NotNull Long userId,
        String userName,
        @NotNull Long transportId,
        @NotNull String transportType,
        @NotNull String status,
        @NotNull LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal totalCost,
        Integer durationMinutes
) {}
