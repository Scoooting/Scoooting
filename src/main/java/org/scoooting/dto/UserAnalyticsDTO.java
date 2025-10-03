package org.scoooting.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record UserAnalyticsDTO(
        Long userId,
        String email,
        String name,
        Integer rentalCount,
        BigDecimal totalSpent,
        Double avgDuration
) {}