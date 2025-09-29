package org.scoooting.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateElectricKickScooterRequestDTO(
        @NotBlank @Size(max = 100) String model,
        @DecimalMin("0") @DecimalMax("100") BigDecimal batteryLevel,
        @Positive @Max(50) Integer maxSpeed,
        @Valid CreateTransportRequestDTO transport
) {}

