package org.scoooting.files.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LocalTimeDto(
        @NotBlank int year,
        @NotBlank int month,
        @NotBlank int day,
        @NotBlank int hour,
        @NotBlank int minute,
        @NotBlank int second
) {}
