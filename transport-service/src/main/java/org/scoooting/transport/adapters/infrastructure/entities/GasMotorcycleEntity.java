package org.scoooting.transport.adapters.infrastructure.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("gas_motorcycles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GasMotorcycle {

    @Id
    private Long transportId;

    @NotBlank
    @Size(max = 100)
    private String model;

    @DecimalMin(value = "0.0", message = "Fuel level cannot be negative")
    @DecimalMax(value = "100.0", message = "Fuel level cannot exceed 100")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal fuelLevel;

    @Positive
    @Min(value = 50, message = "Engine size must be at least 50cc")
    @Max(value = 2000, message = "Engine size cannot exceed 2000cc")
    private Integer engineSize;
}
