package org.scoooting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoooting.entities.enums.MotorcycleStatus;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MotorcycleDTO {
    private Long id;
    private String model;
    private MotorcycleStatus status;
    private Double latitude;
    private Double longitude;
    private Integer engineSize;
    private BigDecimal fuelLevel;
}