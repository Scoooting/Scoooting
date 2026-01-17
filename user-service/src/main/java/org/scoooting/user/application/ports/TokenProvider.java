package org.scoooting.user.application.ports;

import org.scoooting.user.application.dto.response.AuthResult;

public interface TokenProvider {

    AuthResult generate(Long id, String name, String email, String role);

    String getSubject(String token);

    <T> T getClaims(String token, String claim, Class<T> clas);

    boolean validate(String token);

}
