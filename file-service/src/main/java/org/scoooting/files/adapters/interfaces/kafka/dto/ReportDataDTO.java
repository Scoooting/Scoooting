package org.scoooting.files.adapters.interfaces.kafka.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.ToString;

@Builder
public record ReportDataDTO(
        @NotNull(message = "Rental ID cannot be null")
        Long rentalId,

        @NotNull(message = "User ID cannot be null")
        Long userId,

        @NotBlank(message = "Username cannot be blank")
        String username,

        @NotBlank(message = "Email cannot be blank")
        String email,

        @NotBlank(message = "Transport cannot be blank")
        String transport,

        @NotNull(message = "Start time cannot be null")
        @Min(value = 0, message = "Start time must be positive")
        Long startTime,

        @NotNull(message = "End time cannot be null")
        @Min(value = 0, message = "End time must be positive")
        Long endTime,

        @NotNull(message = "Duration cannot be null")
        @Min(value = 1, message = "Duration must be positive")
        Integer durationMinutes,

        @NotBlank(message = "Status cannot be blank")
        String status,

        @NotNull(message = "Total cost cannot be null")
        @Min(value = 0, message = "Total cost must be non-negative")
        Integer totalCost
) {}
