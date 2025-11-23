package org.scoooting.rental.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
public class FeignJwtInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // Попытка получить токен из текущего HTTP запроса
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String authHeader = attributes.getRequest().getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    log.debug("Feign: Forwarding Authorization header to downstream service");
                    template.header("Authorization", authHeader);
                    return;
                }
            }
        } catch (Exception e) {
            log.warn("Feign: Could not extract Authorization header: {}", e.getMessage());
        }

        log.warn("Feign: No Authorization header found to forward");
    }
}