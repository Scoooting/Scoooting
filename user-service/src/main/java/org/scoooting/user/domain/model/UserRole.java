package org.scoooting.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRole {

    private Long id;
    private String name; // "USER", "ADMIN", "OPERATOR", "SUPPORT"

}
