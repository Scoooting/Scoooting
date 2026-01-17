package org.scoooting.user.domain.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class User {

    private Long id;
    private String email;
    private String name;
    private String passwordHash;
    private Long roleId;
    private Long cityId;
    private Integer bonuses;

}
