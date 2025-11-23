package org.scoooting.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter implements WebFilter {

    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        log.info("Gateway filter: incoming request to path: {}", path);

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null) {
            log.info("Gateway filter: No Authorization header, passing through for path: {}", path);
            return chain.filter(exchange);
        }

        if (!authHeader.startsWith("Bearer ")) {
            log.warn("Gateway filter: Authorization header doesn't start with 'Bearer ', value: {}", authHeader);
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);
        log.info("Gateway filter: Extracted token (first 20 chars): {}...", token.substring(0, Math.min(20, token.length())));

        boolean isValid = jwtService.validateJwtToken(token);
        log.info("Gateway filter: Token validation result: {}", isValid);

        if (!isValid) {
            log.error("Gateway filter: Token is INVALID, returning 401 for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        log.info("Gateway filter: Token is VALID, setting authentication and passing request");

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }
}