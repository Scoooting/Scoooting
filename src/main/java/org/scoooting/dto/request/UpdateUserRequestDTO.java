package org.scoooting.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateUserRequestDTO(
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @JsonProperty("cityName")
        String cityName,

        @Min(value = 0, message = "Bonuses cannot be negative")
        Integer bonuses
) {}
