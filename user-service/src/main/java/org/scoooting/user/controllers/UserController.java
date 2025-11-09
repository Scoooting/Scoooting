package org.scoooting.user.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.scoooting.user.dto.common.PageResponseDTO;
import org.scoooting.user.dto.request.UpdateUserRequestDTO;
import org.scoooting.user.dto.response.UserResponseDTO;
import org.scoooting.user.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping("/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(
            @PathVariable @Email(message = "Invalid email format") String email
    ) {
        UserResponseDTO user = userService.findUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    /**
     * "Find-all" request with pagination
     *
     * @param email
     * @param name
     * @param page
     * @param size
     * @return page of defined size containing users
     */
    @GetMapping("/get-users")
    public ResponseEntity<PageResponseDTO<UserResponseDTO>> getUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer size
    ) {
        PageResponseDTO<UserResponseDTO> users = userService.getUsers(email, name, page, size);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(users.totalElements()))
                .header("X-Total-Pages", String.valueOf(users.totalPages()))
                .body(users);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update-user/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestDTO request
    ) {
        UserResponseDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
