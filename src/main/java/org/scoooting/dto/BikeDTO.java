package org.scoooting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoooting.entities.enums.BikeStatus;

public record BikeDTO (Long id, String model, BikeStatus status, Double latitude, Double longitude, Integer gearCount,
                       Boolean isElectric) {}