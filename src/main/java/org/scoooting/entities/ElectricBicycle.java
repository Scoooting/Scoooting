package org.scoooting.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("electric_bicycles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElectricBicycle {

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
    @Min(value = 1, message = "Gear count must be at least 1")
    @Max(value = 30, message = "Gear count cannot exceed 30")
    private Integer gearCount;
}
