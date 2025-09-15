package org.scoooting.entities;

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
    private String model;
    private MotorcycleStatus status;
    private Double latitude;
    private Double longitude;
    private Integer engineSize; // in cc
    private BigDecimal fuelLevel; // percentage
}