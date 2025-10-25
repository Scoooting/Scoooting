package org.scoooting.transport.clients.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

<<<<<<< HEAD:transport-service/src/main/java/org/scoooting/transport/clients/UserClient.java
@FeignClient(name = "user-service", url = "${user-service.url:}", path = "/api")
public interface UserClient {
=======
@FeignClient(name = "user-service", path = "/api")
public interface FeignUserClient {
>>>>>>> d0bac29 (:hammer: feat(transport-service)!: Circuit breaker):transport-service/src/main/java/org/scoooting/transport/clients/feign/FeignUserClient.java

    @GetMapping("/cities/city/{id}")
    ResponseEntity<String> getCityById(@PathVariable("id") Long id);
}
