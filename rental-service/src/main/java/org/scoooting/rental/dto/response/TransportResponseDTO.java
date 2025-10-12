package org.scoooting.rental.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TransportResponseDTO(
        @NotNull Long id,
        @NotNull String typeId,
        @NotNull String statusId,
        @NotNull Double latitude,
        @NotNull Double longitude,
        String cityName
) {}

