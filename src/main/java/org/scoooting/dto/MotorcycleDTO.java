package org.scoooting.dto;

import lombok.Builder;
import org.scoooting.entities.enums.MotorcycleStatus;

import java.math.BigDecimal;

@Builder
public record MotorcycleDTO(Long id, String model, MotorcycleStatus status, Double latitude, Double longitude,
                            Integer engineSize, BigDecimal fuelLevel) {}
