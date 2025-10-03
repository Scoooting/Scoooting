package org.scoooting.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransportAnalyticsDTO(
        Long transportId,
        String model,
        String transportType,
        Double latitude,
        Double longitude,
        Integer totalRentals,
        Integer totalMinutesUsed,
        BigDecimal totalRevenue,
        Double avgRentalDuration,
        LocalDateTime lastRentalTime,
        String usageCategory
) {}
