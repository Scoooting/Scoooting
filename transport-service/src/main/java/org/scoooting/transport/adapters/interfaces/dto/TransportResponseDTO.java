package org.scoooting.transport.adapters.interfaces.dto;

import jakarta.validation.constraints.NotNull;

public record TransportResponseDTO(
        @NotNull Long id,
        @NotNull String type,
        @NotNull String status,
        @NotNull Double latitude,
        @NotNull Double longitude,
        String cityName
) {}

