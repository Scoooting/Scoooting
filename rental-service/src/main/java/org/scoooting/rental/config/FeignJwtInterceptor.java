// rental-service/src/main/java/org/scoooting/rental/config/FeignJwtInterceptor.java

package org.scoooting.rental.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeignJwtInterceptor implements RequestInterceptor {

    private final ServiceAccountJwtProvider serviceAccountJwtProvider;

    public static void setUserToken(String token) {
//        USER_TOKEN.set(token);
//        USE_SERVICE_ACCOUNT.set(false);
    }

    public static void useServiceAccount() {
//        USER_TOKEN.remove();
//        USE_SERVICE_ACCOUNT.set(true);
    }

    public static void clear() {
//        USER_TOKEN.remove();
//        USE_SERVICE_ACCOUNT.remove();
    }

    @Override
    public void apply(RequestTemplate template) {
        String serviceToken = "Bearer " + serviceAccountJwtProvider.getServiceAccountToken();
        template.header("Authorization", serviceToken);
        log.debug("Using service account token for Feign call");
    }
}