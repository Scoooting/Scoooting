package org.scoooting.transport.dto.common;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseDTO(
        @NotNull String message,
        @NotNull String code,
        @NotNull LocalDateTime timestamp,
        Map<String, String> details
) {}
