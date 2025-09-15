package org.scoooting.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AreaAnalyticsRequest {
    private Double centerLatitude;
    private Double centerLongitude;
    private Double radiusMeters;
    private LocalDateTime startDate;
}
