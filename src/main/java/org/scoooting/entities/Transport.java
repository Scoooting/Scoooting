package org.scoooting.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank
    @Size(max = 64)
    private String model;

    @NotBlank
    @Size(max = 32)
    private TransportType type;

    @NotBlank
    @Size(max = 32)
    private TransportStatus status;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private BigDecimal batteryLevel; // For electric vehicles
    private String serialNumber;
    private LocalDateTime lastMaintenanceDate;
}