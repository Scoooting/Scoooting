package org.scoooting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ScooterAnalyticsDTO {
    private Long scooterId;
    private String model;
    private Double latitude;
    private Double longitude;
    private Integer totalRentals;
    private Integer totalMinutesUsed;
    private BigDecimal totalRevenue;
    private Double avgRentalDuration;
    private LocalDateTime lastRentalTime;
    private String usageCategory;
}