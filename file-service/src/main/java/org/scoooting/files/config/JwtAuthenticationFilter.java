package org.scoooting.files.config;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        log.info("UserService JwtAuthenticationFilter: Processing request to URI: {}", requestUri);

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null) {
            log.info("UserService JwtAuthenticationFilter: No Authorization header found for URI: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            log.warn("UserService JwtAuthenticationFilter: Authorization header doesn't start with 'Bearer ': {}", authHeader);
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        log.info("UserService JwtAuthenticationFilter: Extracted JWT token (first 20 chars): {}...",
                token.substring(0, Math.min(20, token.length())));

        try {
            log.info("UserService JwtAuthenticationFilter: Token is VALID");

            String email = jwtService.getEmailFromToken(token);
            String role = jwtService.getRoleFromToken(token);
            Long userId = jwtService.getUserIdFromToken(token);

            log.info("UserService JwtAuthenticationFilter: Extracted from token - userId: {}, email: {}, role: {}",
                    userId, email, role);

            UserPrincipal principal = new UserPrincipal(userId, email, role);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("UserService JwtAuthenticationFilter: Authentication set in SecurityContext successfully");
        } catch (ExpiredJwtException e) {
            log.error("UserService JwtAuthenticationFilter: Token is EXPIRED: {}", e.getMessage());
        } catch (Exception e) {
            log.error("UserService JwtAuthenticationFilter: Exception during token processing: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}