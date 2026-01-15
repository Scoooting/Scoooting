package org.scoooting.user.application.ports;

public interface Password {

    boolean matches(String raw, String hashed);
    String encode(String raw);
}
