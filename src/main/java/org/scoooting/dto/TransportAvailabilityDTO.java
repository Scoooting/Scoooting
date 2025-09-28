package org.scoooting.dto;

import org.scoooting.entities.enums.TransportType;

import java.math.BigDecimal;

public record TransportAvailabilityDTO(
        Long id,
        String model,
        TransportType type,
        String availabilityStatus, // AVAILABLE, IN_USE, UNAVAILABLE, LOW_BATTERY
        Double latitude,
        Double longitude,
        BigDecimal batteryLevel
) {}