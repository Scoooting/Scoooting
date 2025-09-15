package org.scoooting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoooting.entities.enums.TransportStatus;
import org.scoooting.entities.enums.TransportType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransportDTO {
    private Long id;
    private String model;
    private TransportType type;
    private TransportStatus status;
    private Double latitude;
    private Double longitude;
    private BigDecimal batteryLevel;
    private String serialNumber;
    private LocalDateTime lastMaintenanceDate;
}
