package org.scoooting.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("maintenance_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRecord {

    @Id
    private Long id;

    @NotNull
    private Long transportId; // FK to transports

    @NotNull
    private Long operatorId; // FK to users (who performed maintenance)

    @NotBlank
    @Size(max = 100)
    private String maintenanceType; // "BATTERY_REPLACEMENT", "TIRE_CHANGE", etc.

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @DecimalMin(value = "0.0", message = "Cost cannot be negative")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal cost;

    @NotNull
    @PastOrPresent(message = "Maintenance date cannot be in the future")
    private LocalDateTime performedAt;
}
