package org.scoooting.rental.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class FeignJwtInterceptor implements RequestInterceptor {

    // ThreadLocal для передачи токена между реактивными потоками и Feign (blocking)
    private static final ThreadLocal<String> AUTH_TOKEN = new InheritableThreadLocal<>();

    public static void setAuthToken(String token) {
        AUTH_TOKEN.set(token);
    }

    public static void clearAuthToken() {
        AUTH_TOKEN.remove();
    }

    @Override
    public void apply(RequestTemplate template) {
        String token = AUTH_TOKEN.get();
        if (token != null) {
            log.debug("Feign: Forwarding Authorization header");
            template.header("Authorization", token);
        } else {
            log.warn("Feign: No Authorization token available");
        }
    }
}