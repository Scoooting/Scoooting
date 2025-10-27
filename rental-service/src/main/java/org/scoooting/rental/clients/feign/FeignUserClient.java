package org.scoooting.rental.clients.feign;

import jakarta.validation.Valid;
import org.scoooting.rental.dto.request.UpdateUserRequestDTO;
import org.scoooting.rental.dto.response.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", url = "${user-service.url:}", path = "/api/users")
public interface FeignUserClient {

    @GetMapping("/user/{id}")
    ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id);

    @PutMapping("/update-user/{id}")
    ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestDTO request
    );

    @GetMapping("/city/{name}")
    ResponseEntity<Long> getIdByCity(@PathVariable String name);
}
