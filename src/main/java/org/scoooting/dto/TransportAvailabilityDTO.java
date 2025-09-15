package org.scoooting.dto;

import lombok.Builder;
import lombok.Data;
import org.scoooting.entities.enums.TransportType;

import java.math.BigDecimal;

@Data
@Builder
public class TransportAvailabilityDTO {
    private Long id;
    private String model;
    private TransportType type;
    private String availabilityStatus; // AVAILABLE, IN_USE, UNAVAILABLE, LOW_BATTERY
    private Double latitude;
    private Double longitude;
    private BigDecimal batteryLevel;
}