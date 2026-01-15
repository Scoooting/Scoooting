package org.scoooting.rental.adapters.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private String username;
    private Long userId;
    private String email;
    private String role;
}