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

    private static final ThreadLocal<String> USER_TOKEN = new InheritableThreadLocal<>();
    private static final ThreadLocal<Boolean> USE_SERVICE_ACCOUNT = new InheritableThreadLocal<>();

    public static void setUserToken(String token) {
        USER_TOKEN.set(token);
        USE_SERVICE_ACCOUNT.set(false);
    }

    public static void useServiceAccount() {
        USER_TOKEN.remove();
        USE_SERVICE_ACCOUNT.set(true);
    }

    public static void clear() {
        USER_TOKEN.remove();
        USE_SERVICE_ACCOUNT.remove();
    }

    @Override
    public void apply(RequestTemplate template) {
        if (Boolean.TRUE.equals(USE_SERVICE_ACCOUNT.get())) {
            String serviceToken = "Bearer " + serviceAccountJwtProvider.getServiceAccountToken();
            template.header("Authorization", serviceToken);
            log.debug("Using service account token for Feign call");
        } else {
            String userToken = USER_TOKEN.get();
            if (userToken != null) {
                template.header("Authorization", userToken);
                log.debug("Using user token for Feign call");
            }
        }
    }
}