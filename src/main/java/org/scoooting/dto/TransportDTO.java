package org.scoooting.dto;

import lombok.Builder;
import org.scoooting.entities.enums.TransportStatus;
import org.scoooting.entities.enums.TransportType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransportDTO(
        Long id,
        String model,
        TransportType type,
        TransportStatus status,
        Float latitude,
        Float longitude,
        BigDecimal batteryLevel,
        String serialNumber,
        LocalDateTime lastMaintenanceDate
) {}
