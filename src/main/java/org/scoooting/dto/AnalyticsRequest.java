package org.scoooting.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnalyticsRequest {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer minRentals;
    private Integer limit;
}