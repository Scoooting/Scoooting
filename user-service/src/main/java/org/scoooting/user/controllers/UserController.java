package org.scoooting.user.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.user.config.UserPrincipal;
import org.scoooting.user.dto.common.PageResponseDTO;
import org.scoooting.user.dto.request.AdminUpdateUserRequestDTO;
import org.scoooting.user.dto.request.UpdateUserRequestDTO;
import org.scoooting.user.dto.request.UserCreationByAdminRequestDTO;
import org.scoooting.user.dto.response.UserResponseDTO;
import org.scoooting.user.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "[USER] Get current user profile",
            description = "Available to: ALL authenticated users",
            tags = {"User Operations"}
    )
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("UserController: /me endpoint called");

        if (principal == null) {
            log.error("UserController: Principal is NULL!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("UserController: User authenticated - userId: {}, email: {}, role: {}",
                principal.getUserId(), principal.getEmail(), principal.getRole());

        UserResponseDTO user = userService.findUserById(principal.getUserId());
        log.info("UserController: Returning user data for userId: {}", principal.getUserId());
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "[USER] Update current user profile",
            description = "User can only update their own name and city. Available to: ALL authenticated users",
            tags = {"User Operations"}
    )
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateUserRequestDTO request
    ) {
        UserResponseDTO user = userService.updateUser(principal.getUserId(), request);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "[STAFF] Get user by email",
            description = "Available to: SUPPORT, ANALYST, ADMIN",
            tags = {"Staff Operations"}
    )
    @GetMapping("/{email}")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ANALYST', 'ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserByEmail(
            @PathVariable @Email(message = "Invalid email format") String email
    ) {
        UserResponseDTO user = userService.findUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "[STAFF] Get user by ID",
            description = "Available to: SUPPORT, ANALYST, ADMIN",
            tags = {"Staff Operations"}
    )
    @GetMapping("/user/{id}")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ANALYST', 'ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "[STAFF] Get all users with pagination",
            description = "Available to: SUPPORT, ANALYST, ADMIN",
            tags = {"Staff Operations"}
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPPORT', 'ANALYST', 'ADMIN')")
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

    @Operation(
            summary = "[ADMIN] Update any user",
            description = "Admin can update all user fields including role. Available to: ADMIN only",
            tags = {"Admin Operations"}
    )
    @PutMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> adminUpdateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequestDTO request
    ) {
        UserResponseDTO user = userService.adminUpdateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "[ADMIN] Delete user",
            description = "Permanently delete user. Available to: ADMIN only",
            tags = {"Admin Operations"}
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "[SUPPORT] Add or deduct bonuses",
            description = "Support can add/deduct bonuses for compensation or rewards. Available to: SUPPORT, ADMIN",
            tags = {"Support Operations"}
    )
    @PostMapping("/user/{id}/bonuses")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN')")
    public ResponseEntity<UserResponseDTO> addBonuses(
            @PathVariable Long id,
            @RequestParam Integer amount,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserResponseDTO user = userService.addBonuses(id, amount, principal.getUserId());
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "[ADMIN] Create user with specific role",
            description = "Create new user with Operator/Support/Analyst role. Available to: ADMIN only",
            tags = {"Admin Operations"}
    )
    @PostMapping("/admin/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUserWithRole(
            @RequestBody @Valid UserCreationByAdminRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("AuthController: Admin {} creating user with email: {} and role: {}",
                principal.getEmail(), request.email(), request.roleName());
        try {
            String token = userService.createUserWithRole(request);
            log.info("AuthController: User created successfully");
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            log.error("AuthController: Error creating user: {}", e.getMessage(), e);
            throw e;
        }
    }

}