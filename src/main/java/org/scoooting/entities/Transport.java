package org.scoooting.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoooting.entities.enums.TransportType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Table("transports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transport {

    @Id
    private Long id;

    // ENUM stored as VARCHAR
    @NotNull
    private TransportType transportType;

    // FK to transport_statuses table
    @NotNull
    private Long statusId;

    // FK to cities table (nullable - transport can be outside cities)
    private Long cityId;

    @NotNull
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
}