package org.scoooting.transport.adapters.infrastructure.exceptions.dto;

import jakarta.validation.constraints.NotNull;

public record ValidationErrorDTO(
        @NotNull String field,
        @NotNull String message,
        Object rejectedValue
) {}