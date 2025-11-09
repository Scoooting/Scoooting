package org.scoooting.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RentalResponseDTO(
        @NotNull Long id,
        @NotNull Long userId,
        @NotNull Long transportId,
        @NotNull LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal totalCost,
        Integer durationMinutes
) {}