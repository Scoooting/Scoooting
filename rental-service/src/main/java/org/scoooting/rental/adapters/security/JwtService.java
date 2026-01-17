package org.scoooting.rental.adapters.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String getUsernameFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token expired, returning claims anyway");
            return e.getClaims();
        }
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateServiceToken() {
        Date date = Date.from(LocalDateTime.now()
                .plusHours(1)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        return Jwts.builder()
                .subject("rental-service")
                .claims(Map.of(
                        "userId", -1L,  // special ID for service account
                        "email", "rental-service@internal",
                        "role", "OPERATOR"  // OPERATOR role
                ))
                .issuedAt(Date.from(Instant.now()))
                .expiration(date)
                .signWith(getSignInKey())
                .compact();
    }
}