package org.scoooting.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoooting.entities.enums.MotorcycleStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("motorcycles")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Motorcycle {
    @Id
    private Long id;

    @NotBlank
    @Size(max = 64)
    private String model;

    @NotBlank
    @Size(max = 32)
    private MotorcycleStatus status;

    @NotNull
    private Float latitude;

    @NotNull
    private Float longitude;
    private Integer engineSize; // in cc
    private BigDecimal fuelLevel; // percentage
}