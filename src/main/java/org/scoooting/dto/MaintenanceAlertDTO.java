package org.scoooting.dto;

import lombok.Builder;
import lombok.Data;

@Builder
public record MaintenanceAlertDTO(Long scooterId, String model, Double latitude, Double longitude, Integer rentalCount,
                                  Integer totalUsageMinutes) {}
