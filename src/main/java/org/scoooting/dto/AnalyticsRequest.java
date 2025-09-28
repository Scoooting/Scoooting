package org.scoooting.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AnalyticsRequest (
        @NotNull
        LocalDateTime startDate,

        @NotNull
        LocalDateTime endDate,

        @NotNull
        Integer minRentals,

        @NotNull
        Integer limit
) {}