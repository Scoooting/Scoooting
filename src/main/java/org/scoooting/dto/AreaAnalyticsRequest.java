package org.scoooting.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AreaAnalyticsRequest(
        @NotNull
        Double centerLatitude,

        @NotNull
        Double centerLongitude,

        @NotNull
        Double radiusMeters,

        @NotNull
        LocalDateTime startDate
) {}
