package org.scoooting.files.application.ports.dto;

import jakarta.validation.constraints.NotBlank;

public record LocalTimeDto(
        @NotBlank int year,
        @NotBlank int month,
        @NotBlank int day,
        @NotBlank int hour,
        @NotBlank int minute,
        @NotBlank int second
) {}
