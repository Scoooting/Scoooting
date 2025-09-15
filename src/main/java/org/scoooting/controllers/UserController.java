package org.scoooting.controllers;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.scoooting.dto.UserDTO;
import org.scoooting.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getUsers(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        List<UserDTO> users = userService.getUsers(limit, offset);
        long totalCount = userService.getTotalUserCount();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(totalCount))
                .body(users);
    }
}
