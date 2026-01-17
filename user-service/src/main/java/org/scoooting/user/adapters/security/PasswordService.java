package org.scoooting.user.adapters.security;

import lombok.RequiredArgsConstructor;
import org.scoooting.user.application.ports.Password;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordService implements Password {

    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean matches(String nonHashed, String hashed) {
        return passwordEncoder.matches(nonHashed, hashed);
    }

    @Override
    public String encode(String raw) {
        return passwordEncoder.encode(raw);
    }
}
