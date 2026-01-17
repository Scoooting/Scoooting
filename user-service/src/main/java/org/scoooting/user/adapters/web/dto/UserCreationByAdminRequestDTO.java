package org.scoooting.user.adapters.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreationByAdminRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 64, message = "Email must be at most 64 characters")
        String email,

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "Role name is required")
        @Pattern(regexp = "USER|OPERATOR|SUPPORT|ANALYST|ADMIN", message = "Invalid role. Must be one of: USER, OPERATOR, SUPPORT, ANALYST, ADMIN")
        String roleName,

        String cityName
) {}