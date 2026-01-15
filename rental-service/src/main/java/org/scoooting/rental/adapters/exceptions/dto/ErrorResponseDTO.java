package org.scoooting.rental.adapters.exceptions.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseDTO(
        @NotNull String message,
        @NotNull String code,
        @NotNull LocalDateTime timestamp,
        Map<String, String> details
) {}
