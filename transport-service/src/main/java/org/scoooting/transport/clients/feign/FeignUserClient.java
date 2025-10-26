package org.scoooting.transport.clients.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user-service.url:}", path = "/api")
public interface FeignUserClient {

    @GetMapping("/cities/city/{id}")
    ResponseEntity<String> getCityById(@PathVariable("id") Long id);
}
