package org.scoooting.user.domain.model;

import lombok.Data;

@Data
public class UserRole {

    private Long id;
    private String name; // "USER", "ADMIN", "OPERATOR", "SUPPORT"

}
