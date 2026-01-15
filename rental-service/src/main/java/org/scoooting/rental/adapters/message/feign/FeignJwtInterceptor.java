// rental-service/src/main/java/org/scoooting/rental/config/FeignJwtInterceptor.java

package org.scoooting.rental.adapters.message.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.rental.adapters.security.ServiceAccountJwtProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeignJwtInterceptor implements RequestInterceptor {

    private final ServiceAccountJwtProvider serviceAccountJwtProvider;

    @Override
    public void apply(RequestTemplate template) {
        String serviceToken = "Bearer " + serviceAccountJwtProvider.getServiceAccountToken();
        template.header("Authorization", serviceToken);
        log.debug("Using service account token for Feign call");
    }
}