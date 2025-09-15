package org.scoooting.entities;

import lombok.Data;
import org.scoooting.entities.enums.UserRoles;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "users")
@Data
public class User {

    @Id
    private Long id;
    private String email;
    private String name;
    private String password;
    private Integer bonuses;
    private UserRoles role;

}
