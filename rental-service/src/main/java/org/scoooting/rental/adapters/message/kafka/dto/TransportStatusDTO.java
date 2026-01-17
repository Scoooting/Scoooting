package org.scoooting.rental.adapters.message.kafka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransportStatusDTO(
    @NotNull Long transportId,
    @NotBlank String status
) {}