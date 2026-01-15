package org.scoooting.user.adapters.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationDTO(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 2, max = 100) String name,
        @NotBlank @Size(min = 8) String password,
        String cityName
) {}
