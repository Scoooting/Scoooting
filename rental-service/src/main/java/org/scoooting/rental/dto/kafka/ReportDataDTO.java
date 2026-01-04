package org.scoooting.rental.dto.kafka;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ReportDataDTO(
        @NotNull Long rentalId,
        @NotNull Long userId,
        @NotBlank String username,
        @NotBlank String email,
        @NotBlank String transport,
        @NotBlank Long startTime,
        @NotBlank Long endTime,
        @NotNull Integer durationMinutes,
        @NotBlank String status,
        @NotNull Integer totalCost
) {}
