package org.scoooting.files.application.ports.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record LocalTimeDto(
        @Min(value = 2020, message = "Year must be >= 2020")
        @Max(value = 2099, message = "Year must be <= 2099")
        int year,

        @Min(value = 1, message = "Month must be between 1-12")
        @Max(value = 12, message = "Month must be between 1-12")
        int month,

        @Min(value = 1, message = "Day must be between 1-31")
        @Max(value = 31, message = "Day must be between 1-31")
        int day,

        @Min(value = 0, message = "Hour must be between 0-23")
        @Max(value = 23, message = "Hour must be between 0-23")
        int hour,

        @Min(value = 0, message = "Minute must be between 0-59")
        @Max(value = 59, message = "Minute must be between 0-59")
        int minute,

        @Min(value = 0, message = "Second must be between 0-59")
        @Max(value = 59, message = "Second must be between 0-59")
        int second
) {}