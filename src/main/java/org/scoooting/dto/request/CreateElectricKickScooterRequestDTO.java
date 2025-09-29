package org.scoooting.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateElectricKickScooterRequestDTO(
        @NotBlank @Size(max = 100) String model,
        @DecimalMin("0") @DecimalMax("100") @JsonProperty("batteryLevel") BigDecimal batteryLevel,
        @Positive @Max(50) @JsonProperty("maxSpeed") Integer maxSpeed,
        @Valid CreateTransportRequestDTO transport
) {}

