package org.scoooting.transport.adapters.interfaces.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.scoooting.transport.domain.model.enums.TransportType;

public record CreateTransportRequestDTO(
        @NotNull TransportType type,
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
        String cityName
) {}

