package org.scoooting.transport.adapters.infrastructure.messaging.kafka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransportStatusDTO(
        @NotNull Long transportId,
        @NotBlank String status
) {}