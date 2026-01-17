package org.scoooting.user.domain.model;

import lombok.Data;

@Data
public class RefreshToken {
    private Long userId;
    private String token;
}
