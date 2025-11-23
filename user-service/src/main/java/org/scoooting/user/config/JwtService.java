package org.scoooting.user.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.user.dto.JwtDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


@Slf4j
@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-duration}")
    private int accessDuration;

    @Value("${jwt.refresh-duration}")
    private int refreshDuration;

    public JwtDto generateAuthToken(Long userId, String email, String role) {
        log.info("JwtService: Generating auth tokens for userId: {}, email: {}, role: {}", userId, email, role);
        return new JwtDto(
                generateJwtToken(userId, email, role),
                generateRefreshToken(userId, email, role)
        );
    }

    public String refreshAuthToken(Long userId, String email, String role) {
        log.info("JwtService: Refreshing auth token for userId: {}, email: {}", userId, email);
        return generateJwtToken(userId, email, role);
    }

    public String getEmailFromToken(String token) {
        String email = getClaims(token).getSubject();
        log.debug("JwtService: Extracted email from token: {}", email);
        return email;
    }

    public String getRoleFromToken(String token) {
        String role = getClaims(token).get("role", String.class);
        log.debug("JwtService: Extracted role from token: {}", role);
        return role;
    }

    public Long getUserIdFromToken(String token) {
        Long userId = getClaims(token).get("userId", Long.class);
        log.debug("JwtService: Extracted userId from token: {}", userId);
        return userId;
    }

    public boolean validateJwtToken(String token) {
        if (token == null) {
            log.warn("JwtService: Token is null");
            return false;
        }

        try {
            log.debug("JwtService: Validating token...");
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            log.info("JwtService: Token validation SUCCESSFUL");
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JwtService: Token is EXPIRED: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("JwtService: Token is MALFORMED: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JwtService: Token validation failed: {}", e.getMessage(), e);
        }

        return false;
    }

    private String generateJwtToken(Long userId, String email, String role) {
        Date date = Date.from(LocalDateTime.now()
                .plusSeconds(accessDuration)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        String token = Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .expiration(date)
                .signWith(getSignInKey())
                .compact();

        log.info("JwtService: Generated JWT token for userId: {}, expires at: {}", userId, date);
        return token;
    }

    private String generateRefreshToken(Long userId, String email, String role) {
        Date date = Date.from(LocalDateTime.now()
                .plusSeconds(refreshDuration)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        String token = Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .expiration(date)
                .signWith(getSignInKey())
                .compact();

        log.info("JwtService: Generated refresh token for userId: {}, expires at: {}", userId, date);
        return token;
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JwtService: Token expired, returning claims anyway: {}", e.getMessage());
            return e.getClaims();
        }
    }

    private SecretKey getSignInKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            log.debug("JwtService: Secret key decoded, length: {} bytes", keyBytes.length);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("JwtService: Failed to decode secret key: {}", e.getMessage(), e);
            throw e;
        }
    }
}