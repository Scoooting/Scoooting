package org.scoooting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
public record CityRevenueDTO(String cityName, Integer totalRentals, BigDecimal totalRevenue, Double avgDuration) {}