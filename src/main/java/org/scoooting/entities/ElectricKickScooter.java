package org.scoooting.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("electric_kick_scooters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElectricKickScooter {

    // PK = FK to transports table
    @Id
    private Long transportId;

    @NotBlank
    @Size(max = 100, message = "Model name cannot exceed 100 characters")
    private String model;

    @DecimalMin(value = "0.0", message = "Battery level cannot be negative")
    @DecimalMax(value = "100.0", message = "Battery level cannot exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Battery level format: XXX.XX")
    private BigDecimal batteryLevel;

    @Positive(message = "Max speed must be positive")
    @Max(value = 50, message = "Max speed cannot exceed 50 km/h for kick scooters")
    private Integer maxSpeed;
}