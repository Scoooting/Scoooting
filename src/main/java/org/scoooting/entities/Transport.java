package org.scoooting.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoooting.entities.enums.TransportStatus;
import org.scoooting.entities.enums.TransportType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("transports")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transport {

    @Id
    private Long id;
    private String model;
    private TransportType type;
    private TransportStatus status;
    private Double latitude;
    private Double longitude;
    private BigDecimal batteryLevel; // For electric vehicles
    private String serialNumber;
    private LocalDateTime lastMaintenanceDate;
}