package org.scoooting.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaintenanceAlertDTO {
    private Long scooterId;
    private String model;
    private Double latitude;
    private Double longitude;
    private Integer rentalCount;
    private Integer totalUsageMinutes;
}