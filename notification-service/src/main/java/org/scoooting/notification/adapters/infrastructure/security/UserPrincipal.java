package org.scoooting.notification.adapters.infrastructure.security;

import lombok.AllArgsConstructor;

import java.security.Principal;

@AllArgsConstructor
public class UserPrincipal implements Principal {

    private String name;

    @Override
    public String getName() {
        return name;
    }
}
