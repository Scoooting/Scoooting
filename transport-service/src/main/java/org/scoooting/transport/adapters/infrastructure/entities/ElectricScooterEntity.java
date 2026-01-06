package org.scoooting.transport.adapters.infrastructure.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("electric_scooters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElectricScooter {

    @Id
    private Long transportId;

    @NotBlank
    @Size(max = 100)
    private String model;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal batteryLevel;

    @Positive
    @Max(value = 80, message = "Max speed cannot exceed 80 km/h for electric scooters")
    private Integer maxSpeed;

    @NotNull
    private Boolean hasStorageBox;
}
