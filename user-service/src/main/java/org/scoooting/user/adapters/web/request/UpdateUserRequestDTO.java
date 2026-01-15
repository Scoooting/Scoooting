package org.scoooting.user.adapters.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateUserRequestDTO(
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,
        String cityName
) {}
