package org.scoooting.transport.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api")
public interface UserClient {

    @GetMapping("/cities/city/{id}")
    ResponseEntity<String> getCityById(@PathVariable("id") Long id);
}
