package org.scoooting.entities;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.scoooting.entities.enums.UserRoles;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "users")
@Data
public class User {

    @Id
    private Long id;

    @Email
    @Size(max = 64)
    private String email;

    @NotBlank
    @Size(max = 32)
    private String name;

    @NotBlank
    @Size(max = 128)
    private String password;

    @NotNull
    private Integer bonuses;

    @NotBlank
    @Size(max = 16)
    private UserRoles role;

}
