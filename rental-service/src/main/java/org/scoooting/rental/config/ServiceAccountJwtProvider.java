package org.scoooting.rental.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceAccountJwtProvider {

    private final JwtService jwtService;

    private String cachedToken;
    private Instant tokenExpiry;

    public String getServiceAccountToken() {
        // use cached token if it's valid
        if (cachedToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }

        // Генерируем новый токен
        Instant now = Instant.now();
        Instant expiry = now.plus(1, ChronoUnit.HOURS);

        cachedToken = jwtService.generateServiceToken();

        tokenExpiry = expiry;
        log.info("Generated new service account token, expires at: {}", expiry);

        return cachedToken;
    }
}