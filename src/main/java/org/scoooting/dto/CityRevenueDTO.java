package org.scoooting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CityRevenueDTO {
    private String cityName;
    private Integer totalRentals;
    private BigDecimal totalRevenue;
    private Double avgDuration;
}