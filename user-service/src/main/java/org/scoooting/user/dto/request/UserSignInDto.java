package org.scoooting.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserSignInDto(@Email
                            @NotBlank
                            @Size(max = 64)
                            String email,

                            @NotBlank
                            @Size(min = 6, max = 32)
                            String password) {
}
