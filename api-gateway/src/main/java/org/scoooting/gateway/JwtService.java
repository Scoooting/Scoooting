package org.scoooting.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Slf4j
@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public boolean validateJwtToken(String token) {
        if (token == null) {
            log.warn("JwtService: Token is null");
            return false;
        }

        try {
            log.debug("JwtService: Attempting to validate token...");
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
            log.error("JwtService: Token validation failed with exception: {}", e.getMessage(), e);
        }

        return false;
    }

    private SecretKey getSignInKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            log.debug("JwtService: Secret key decoded successfully, length: {} bytes", keyBytes.length);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("JwtService: Failed to decode secret key: {}", e.getMessage(), e);
            throw e;
        }
    }
}