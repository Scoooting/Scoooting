package org.scoooting.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ScooterAnalyticsDTO(
        Long scooterId,
        String model,
        Double latitude,
        Double longitude,
        Integer totalRentals,
        Integer totalMinutesUsed,
        BigDecimal totalRevenue,
        Double avgRentalDuration,
        LocalDateTime lastRentalTime,
        String usageCategory) {}