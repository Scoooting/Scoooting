package org.scoooting.user.application.dto.response;

public record AuthResult(String accessToken, String refreshToken) {
}
