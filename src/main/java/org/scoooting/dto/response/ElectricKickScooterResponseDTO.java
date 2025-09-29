package org.scoooting.dto.response;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ElectricKickScooterResponseDTO(
        @NotNull Long transportId,
        @NotNull String model,
        BigDecimal batteryLevel,
        Integer maxSpeed,
        @NotNull TransportResponseDTO transport
) {}
