package org.scoooting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserAnalyticsDTO {
    private Long userId;
    private String email;
    private String name;
    private Integer rentalCount;
    private BigDecimal totalSpent;
    private Double avgDuration;
}