package org.scoooting.user.adapters.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record AdminUpdateUserRequestDTO(
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Email(message = "Invalid email format")
        @Size(max = 64, message = "Email must be at most 64 characters")
        String email,

        String cityName,

        @Min(value = 0, message = "Bonuses cannot be negative")
        Integer bonuses,

        String roleName  // Может менять роль
) {}