package org.scoooting.entities;

import lombok.Builder;
import lombok.Data;
import org.scoooting.entities.enums.Roles;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "users")
@Data
@Builder
public class User {

    @Id
    private long id;
    private String email;
    private String name;
    private String password;
    private int bonuses;
    private Roles role;

}
