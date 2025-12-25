package org.scoooting.rental.clients.feign;

import jakarta.validation.Valid;
import org.scoooting.rental.dto.request.UpdateUserRequestDTO;
import org.scoooting.rental.dto.response.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", url = "${user-service.url:}", path = "/api/users")
public interface FeignUserClient {

    @GetMapping("/user/{id}")
    ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id);

    @GetMapping("/me")
    ResponseEntity<UserResponseDTO> getCurrentUser();

    @PostMapping("/user/{id}/bonuses")
    ResponseEntity<UserResponseDTO> addBonuses(
            @PathVariable Long id,
            @RequestParam Integer amount
    );

    @GetMapping("/city/{name}")
    ResponseEntity<Long> getIdByCity(@PathVariable String name);
}
